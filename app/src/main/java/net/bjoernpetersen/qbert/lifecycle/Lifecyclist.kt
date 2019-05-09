package net.bjoernpetersen.qbert.lifecycle

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.errors.IOException
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.auth.BotUser
import net.bjoernpetersen.musicbot.api.auth.DefaultPermissions
import net.bjoernpetersen.musicbot.api.config.ConfigManager
import net.bjoernpetersen.musicbot.api.config.MainConfigScope
import net.bjoernpetersen.musicbot.api.config.PluginConfigScope
import net.bjoernpetersen.musicbot.api.module.BrowserOpenerModule
import net.bjoernpetersen.musicbot.api.module.ConfigModule
import net.bjoernpetersen.musicbot.api.module.DefaultImageCacheModule
import net.bjoernpetersen.musicbot.api.module.DefaultPlayerModule
import net.bjoernpetersen.musicbot.api.module.DefaultQueueModule
import net.bjoernpetersen.musicbot.api.module.DefaultResourceCacheModule
import net.bjoernpetersen.musicbot.api.module.DefaultSongLoaderModule
import net.bjoernpetersen.musicbot.api.module.DefaultUserDatabaseModule
import net.bjoernpetersen.musicbot.api.module.FileStorageModule
import net.bjoernpetersen.musicbot.api.module.InstanceStopper
import net.bjoernpetersen.musicbot.api.module.PluginClassLoaderModule
import net.bjoernpetersen.musicbot.api.module.PluginModule
import net.bjoernpetersen.musicbot.api.player.PlayerState
import net.bjoernpetersen.musicbot.api.player.QueueEntry
import net.bjoernpetersen.musicbot.api.player.Song
import net.bjoernpetersen.musicbot.api.plugin.management.DefaultDependencyManager
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.api.plugin.management.findDependencies
import net.bjoernpetersen.musicbot.spi.player.Player
import net.bjoernpetersen.musicbot.spi.player.QueueChangeListener
import net.bjoernpetersen.musicbot.spi.player.SongQueue
import net.bjoernpetersen.musicbot.spi.plugin.NoSuchSongException
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.PluginLookup
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import net.bjoernpetersen.musicbot.spi.plugin.category
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager
import net.bjoernpetersen.musicbot.spi.plugin.management.InitStateWriter
import net.bjoernpetersen.musicbot.spi.util.BrowserOpener
import net.bjoernpetersen.qbert.impl.Broadcaster
import net.bjoernpetersen.qbert.impl.BundlePluginLoader
import net.bjoernpetersen.qbert.impl.FileConfigStorage
import net.bjoernpetersen.qbert.impl.FileStorageImpl
import net.bjoernpetersen.qbert.impl.ImageLoaderImpl
import net.bjoernpetersen.qbert.impl.MainConfigEntries
import net.bjoernpetersen.qbert.impl.SongPlayedNotifierModule
import net.bjoernpetersen.qbert.rest.KtorServer
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@Suppress("TooManyFunctions")
class Lifecyclist : CoroutineScope {
    private val logger = KotlinLogging.logger {}

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    var stage: Stage = Stage.New
        private set

    // Created stage vars
    private lateinit var rootDir: File
    private lateinit var configManager: ConfigManager
    private lateinit var classLoader: ClassLoader
    private lateinit var dependencyManager: DependencyManager

    // Injected stage vars
    private lateinit var pluginFinder: PluginFinder
    private lateinit var pluginLookup: PluginLookup
    private lateinit var injector: Injector
    private lateinit var mainConfig: MainConfigEntries

    // Run stage vars
    private lateinit var broadcaster: Broadcaster

    private fun <T> stagedBlock(stage: Stage, exact: Boolean = true, action: () -> T): T {
        if (exact) {
            check(this.stage == stage)
        } else {
            check(this.stage >= stage)
        }
        return action()
    }

    private suspend fun <T> staged(
        stage: Stage,
        exact: Boolean = true,
        action: suspend () -> T
    ): T {
        if (exact) {
            check(this.stage == stage)
        } else {
            check(this.stage >= stage)
        }
        return action()
    }

    fun getConfigManager() = stagedBlock(Stage.Created, false) { configManager }
    fun getPluginClassLoader() = stagedBlock(Stage.Created, false) { classLoader }

    fun getDependencyManager() = stagedBlock(
        Stage.Created, false
    ) { dependencyManager }

    fun getPluginFinder() = stagedBlock(Stage.Injected, false) { pluginFinder }
    fun getPluginLookup() = stagedBlock(Stage.Injected, false) { pluginLookup }
    fun getInjector() = stagedBlock(Stage.Injected, false) { injector }
    fun getMainConfig() = stagedBlock(Stage.Injected, false) { mainConfig }

    private fun createConfig(rootDir: File) {
        val stateDir = File(rootDir, "state")
        val configDir = File(rootDir, "config")
        val secretDir = File(configDir, "secret")
        configManager = ConfigManager(
            FileConfigStorage(configDir),
            FileConfigStorage(secretDir),
            FileConfigStorage(stateDir)
        )
    }

    private fun createPlugins() {
        val loader = BundlePluginLoader()
        dependencyManager = DefaultDependencyManager(configManager[MainConfigScope].state, loader)
        classLoader = loader.loader
    }

    fun create(filesDir: File): DependencyManager = stagedBlock(Stage.New) {
        job = Job()
        rootDir = filesDir
        createConfig(filesDir)
        createPlugins()

        stage = Stage.Created
        dependencyManager
    }

    private fun modules(browserOpener: BrowserOpener, suggester: Suggester?): List<Module> = listOf(
        ConfigModule(configManager),
        DefaultPlayerModule(suggester),
        DefaultQueueModule(),
        DefaultSongLoaderModule(),
        DefaultUserDatabaseModule("jdbc:sqlite:${File(rootDir, "UserDatabase.db")}"),
        PluginClassLoaderModule(classLoader),
        PluginModule(pluginFinder),
        BrowserOpenerModule(browserOpener),
        SongPlayedNotifierModule(),
        DefaultImageCacheModule(),
        ImageLoaderImpl,
        DefaultResourceCacheModule(),
        FileStorageModule(FileStorageImpl::class)
    )

    fun inject(browserOpener: BrowserOpener) = stagedBlock(Stage.Created) {
        pluginFinder = dependencyManager.finish()

        mainConfig = MainConfigEntries(configManager, pluginFinder, classLoader)

        val suggester = mainConfig.defaultSuggester.get()
        logger.info { "Default suggester: ${suggester?.name}" }

        injector = Guice.createInjector(modules(browserOpener, suggester))

        pluginFinder.allPlugins().forEach {
            injector.injectMembers(it)
        }

        pluginFinder.allPlugins().forEach {
            val configs = configManager[PluginConfigScope(it::class)]
            it.createConfigEntries(configs.plain)
            it.createSecretEntries(configs.secrets)
            it.createStateEntries(configs.state)
        }

        pluginLookup = injector.getInstance(PluginLookup::class.java)
        stage = Stage.Injected
    }

    suspend fun run(
        writerFactory: (Plugin) -> InitStateWriter,
        result: (Throwable?) -> Unit
    ) = staged(Stage.Injected) {
        // TODO rollback in case of failure
        coroutineScope {
            Initializer(pluginFinder).start(writerFactory) {
                if (it != null) {
                    logger.error(it) { "Could not initialize!" }
                    result(it)
                    return@start
                }
                DefaultPermissions.defaultPermissions = mainConfig.defaultPermissions.get()!!

                injector.getInstance(Player::class.java).start()

                val ktor = injector.getInstance(KtorServer::class.java)
                ktor.start()

                broadcaster = Broadcaster().apply { start() }

                GlobalScope.launch {
                    val dumper = injector.getInstance(QueueDumper::class.java)
                    dumper.restoreQueue()
                    injector.getInstance(Player::class.java)
                        .addListener { _, newState -> dumper.dumpQueue(newState) }
                    injector.getInstance(SongQueue::class.java)
                        .addListener(object : QueueChangeListener {
                            override fun onAdd(entry: QueueEntry) {
                                dumper.dumpQueue()
                            }

                            override fun onMove(entry: QueueEntry, fromIndex: Int, toIndex: Int) {
                                dumper.dumpQueue()
                            }

                            override fun onRemove(entry: QueueEntry) {
                                dumper.dumpQueue()
                            }
                        })
                }

                // TODO DeskBot.runningInstance = this@Lifecyclist
                stage = Stage.Running
                result(null)
            }
        }
    }

    fun stop() = stagedBlock(Stage.Running) {
        try {
            broadcaster.close()
        } catch (e: IOException) {
            logger.error(e) { "Could not close broadcaster" }
        }

        runBlocking {
            coroutineScope {
                withContext(coroutineContext) {
                    val stopper = InstanceStopper(injector).apply {
                        register(KtorServer::class.java) { ktor ->
                            ktor.close()
                        }
                    }
                    stopper.stop()
                    stage = Stage.Stopped
                }
            }

            job.cancel()
        }
    }

    enum class Stage {
        Stopped, New, Created, Injected, Running
    }
}

@Suppress("MagicNumber")
private class Initializer(private val finder: PluginFinder) {

    private val logger = KotlinLogging.logger {}

    suspend fun start(writerFactory: (Plugin) -> InitStateWriter, result: (Throwable?) -> Unit) {
        val lock = Mutex()
        val finished: MutableSet<Plugin> = HashSet(64)
        val errors: MutableList<Throwable> = ArrayList()

        // TODO show loading screen
        logger.info { "Loading..." }

        val jobByPlugin: MutableMap<Plugin, Job> = HashMap(64)
        supervisorScope {
            lock.withLock {
                finder.allPlugins().forEach { plugin ->
                    val writer = writerFactory(plugin)
                    val job = launch {
                        plugin.findDependencies()
                            .asSequence()
                            .map { finder[it]!! }
                            .forEach {
                                val type = it.category.simpleName
                                while (it !in finished) {
                                    lock.withLock { }
                                    logger.info { "Waiting for $type ${it.name}" }
                                    delay(200)
                                }
                            }

                        logger.info { "Starting ${plugin.name}" }
                        try {
                            plugin.initialize(writer)
                        } catch (e: Throwable) {
                            logger.error(e) { "Could not initialize $plugin" }
                            errors.add(e)
                            return@launch
                        }

                        lock.withLock {
                            finished.add(plugin)
                        }

                    }
                    jobByPlugin[plugin] = job
                }
            }
        }
        logger.info { "Done loading." }

        val exception = if (errors.isEmpty()) null
        else errors.fold(Exception("One or more initializations failed")) { e, t ->
            e.apply { addSuppressed(t) }
        }

        result(exception)
    }
}

private class QueueDumper @Inject private constructor(
    private val queue: SongQueue,
    private val player: Player,
    private val pluginLookup: PluginLookup
) {

    private val logger = KotlinLogging.logger {}

    private fun Song.toDumpString() = "${provider.id}|$id\n"

    private var lastQueue: List<QueueEntry> = emptyList()

    private fun buildDumpQueue(
        playerState: PlayerState,
        queue: List<QueueEntry>
    ): List<QueueEntry> {
        val result = ArrayList<QueueEntry>(queue.size)
        val currentEntry = playerState.entry
        if (currentEntry is QueueEntry) {
            // If there is a current song which has not been auto-suggested, prepend it
            logger.debug { "Dumping current song first" }
            result.add(currentEntry)
        } else {
            logger.debug { "Not dumping current song. State: ${player.state}" }
        }
        result.addAll(queue)
        return result
    }

    fun dumpQueue(playerState: PlayerState = player.state) {
        logger.debug { "Dumping queue" }
        val dumpQueue = buildDumpQueue(playerState, queue.toList())
        if (dumpQueue == lastQueue) {
            logger.debug { "Not dumping unchanged queue." }
            return
        } else {
            lastQueue = dumpQueue
        }
        val file = File("queue.dump")
        file.bufferedWriter().use { writer ->
            dumpQueue.forEach {
                writer.write(it.song.toDumpString())
            }
        }
    }

    suspend fun restoreQueue() {
        val file = File("queue.dump")
        if (!file.isFile) return

        logger.info("Restoring queue")
        withContext(Dispatchers.IO) {
            file.bufferedReader().useLines { lines ->
                val songs = lines
                    .map { it.split('|') }
                    .filter { it.size == 2 }
                    .map { pluginLookup.lookup<Provider>(it[0]) to it[1] }
                    .map {
                        async {
                            try {
                                it.first?.lookup(it.second)
                            } catch (e: NoSuchSongException) {
                                null
                            }
                        }
                    }
                    .toList()

                withContext(Dispatchers.IO) {
                    songs.forEach {
                        val song = it.await()
                        if (song != null) {
                            queue.insert(QueueEntry(song, BotUser))
                        }
                    }
                }
            }
        }
    }
}
