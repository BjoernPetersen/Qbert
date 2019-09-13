package net.bjoernpetersen.qbert.impl

import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.config.ActionButton
import net.bjoernpetersen.musicbot.api.config.ChoiceBox
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.config.ConfigManager
import net.bjoernpetersen.musicbot.api.config.ConfigSerializer
import net.bjoernpetersen.musicbot.api.config.FileChooser
import net.bjoernpetersen.musicbot.api.config.FileSerializer
import net.bjoernpetersen.musicbot.api.config.MainConfigScope
import net.bjoernpetersen.musicbot.api.config.NonnullConfigChecker
import net.bjoernpetersen.musicbot.api.config.SerializationException
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import net.bjoernpetersen.musicbot.spi.plugin.id
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class MainConfigEntries @Inject constructor(
    configManager: ConfigManager,
    pluginFinder: PluginFinder,
    @Named("PluginClassLoader")
    classLoader: ClassLoader
) {

    private val plain = configManager[MainConfigScope].plain
    private val secret = configManager[MainConfigScope].secrets

    val defaultSuggester = configManager[MainConfigScope].plain.SerializedEntry(
        key = "defaultSuggester",
        description = "The suggester providing songs if the queue is empty",
        serializer = SuggesterSerializer(classLoader, pluginFinder),
        configChecker = { null },
        uiNode = ChoiceBox({ it.name }, { pluginFinder.suggesters })
    )

    val defaultPermissions: Config.SerializedEntry<Set<Permission>> = plain.SerializedEntry(
        key = "defaultPermissions",
        description = "Permissions for new users and guests",
        serializer = PermissionSetSerializer,
        configChecker = NonnullConfigChecker,
        uiNode = ActionButton("Edit", { it.sorted().joinToString() }) {
            //load<DefaultPermissionConfig>().apply {
            //    configEntry = defaultPermissions()
            //    root.show(modal = true, wait = true)
            //}
            // TODO open permission config window
            true
        },
        default = Permission.getDefaults()
    )

    val storageDir: Config.SerializedEntry<File> = plain.SerializedEntry(
        key = "storageDir",
        description = "Directory to store plugin files in." +
            " This should preferably be somewhere with a lot of free space.",
        serializer = FileSerializer,
        configChecker = {
            if (it != null && it.isDirectory) null
            else "Must be an existing directory"
        },
        uiNode = FileChooser(),
        default = File("storage")
    )

    val loadAlbumArt = plain.SerializedEntry(
        key = "loadAlbumArt",
        description = "Which album art images to load. Disable to save bandwidth.",
        serializer = AlbumArtMode,
        configChecker = NonnullConfigChecker,
        uiNode = ChoiceBox({ it.friendlyName }, { AlbumArtMode.values().asList() }),
        default = AlbumArtMode.ALL
    )

    val allPlain: List<Config.Entry<*>> = listOf(
        defaultSuggester,
        storageDir,
        loadAlbumArt,
        defaultPermissions
    )

    private fun defaultPermissions() = defaultPermissions
}

private class SuggesterSerializer(
    private val classLoader: ClassLoader,
    private val pluginFinder: PluginFinder
) : ConfigSerializer<Suggester> {

    override fun serialize(obj: Suggester): String {
        return obj.id.qualifiedName!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(string: String): Suggester {
        val type = try {
            classLoader.loadClass(string)
        } catch (e: ClassNotFoundException) {
            throw SerializationException()
        } as Class<out Suggester>
        return pluginFinder[type.kotlin] ?: throw SerializationException()
    }
}

private object PermissionSetSerializer : ConfigSerializer<Set<Permission>> {
    private val logger = KotlinLogging.logger { }
    override fun deserialize(string: String): Set<Permission> {
        return if (string == "NONE") emptySet()
        else string
            .split(',')
            .mapNotNull {
                try {
                    Permission.matchByLabel(it)
                } catch (e: IllegalArgumentException) {
                    logger.warn { "Unknown default permission: $it" }
                    null
                }
            }
            .toSet()
    }

    override fun serialize(obj: Set<Permission>): String {
        return if (obj.isEmpty()) "NONE"
        else obj.joinToString(",") { it.label }
    }
}
