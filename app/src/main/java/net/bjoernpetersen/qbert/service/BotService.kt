package net.bjoernpetersen.qbert.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDeepLinkBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.management.InitStateWriter
import net.bjoernpetersen.qbert.R
import net.bjoernpetersen.qbert.android.ONGOING_NOTIFICATION_CHANNEL_ID
import net.bjoernpetersen.qbert.android.ONGOING_NOTIFICATION_ID
import net.bjoernpetersen.qbert.android.intent
import net.bjoernpetersen.qbert.android.pendingActivity
import net.bjoernpetersen.qbert.app
import net.bjoernpetersen.qbert.impl.LogInitStateWriter
import net.bjoernpetersen.qbert.impl.createBrowserOpener
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import net.bjoernpetersen.qbert.view.RunActivity

class BotService : Service(),
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default) {
    private val logger = KotlinLogging.logger {}
    private lateinit var notification: Notification
    private var life = Lifecyclist()
    private val mutStage = MutableLiveData(life.stage)
    private var initJob: Job = CompletableDeferred(true)
    private val writers: MutableMap<Plugin, InitStateWriter> = HashMap()
    private var writerFactory: ((Plugin) -> InitStateWriter)? = null
        set(value) {
            field = value
            writers.clear()
        }

    private fun getWriter(plugin: Plugin): InitStateWriter {
        return writers.computeIfAbsent(plugin) {
            writerFactory?.let { factory -> factory(it) } ?: LogInitStateWriter(it)
        }
    }

    private suspend fun run(life: Lifecyclist) {
        life.create(app.filesDir)
        mutStage.postValue(life.stage)
        life.inject(app.createBrowserOpener())
        mutStage.postValue(life.stage)
        life.run({ DelegationWriter { getWriter(it) } }) {
            // FIXME do something sensible
            it?.printStackTrace()
        }
        mutStage.postValue(life.stage)
    }

    private fun tryStart() {
        if (app.isServiceRunning) return

        initJob = launch { run(life) }

        notification = createNotification()
        startForeground(ONGOING_NOTIFICATION_ID, notification)
        app.isServiceRunning = true
    }

    override fun onBind(intent: Intent): Bot {
        logger.debug("Binding")
        return BotImpl()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        logger.debug("Start service")
        tryStart()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        logger.debug("Destroy service")
        initJob.cancel()
        val job = coroutineContext[Job] as CompletableJob
        job.invokeOnCompletion {
            if (life.stage == Lifecyclist.Stage.Running) life.stop()
            mutStage.postValue(life.stage)
            app.isServiceRunning = false
        }
        job.complete()
        super.onDestroy()
    }

    private inner class BotImpl : Binder(), Bot {
        override val stage: LiveData<Lifecyclist.Stage>
            get() = mutStage

        override fun getLifecyclist(): Lifecyclist = life

        override fun setInitStateWriterFactory(factory: (Plugin) -> InitStateWriter) {
            writerFactory = factory
        }
    }
}

private class DelegationWriter(
    private val getWriter: () -> InitStateWriter
) : InitStateWriter {
    override fun state(state: String) {
        getWriter().state(state)
    }

    override fun warning(warning: String) {
        getWriter().warning(warning)
    }
}


interface Bot : IBinder {
    val stage: LiveData<Lifecyclist.Stage>
    fun getLifecyclist(): Lifecyclist
    fun setInitStateWriterFactory(factory: (Plugin) -> InitStateWriter)
}

private fun Service.createNotification(): Notification =
    Notification.Builder(this, ONGOING_NOTIFICATION_CHANNEL_ID)
        .setContentTitle(getText(R.string.notification_title))
        .setContentText(getText(R.string.notification_message))
        .setSmallIcon(R.drawable.ic_play)
        .setContentIntent(intent<RunActivity>().pendingActivity(this))
        .setTicker(getText(R.string.notification_ticker))
        .setActions(buildOpenAction(), buildStopAction())
        .build()

private fun Service.buildOpenAction(): Notification.Action {
    val openIntent = NavDeepLinkBuilder(this)
        .setGraph(R.navigation.nav_run)
        .setDestination(R.id.playerFragment)
        .setComponentName(RunActivity::class.java)
        .createPendingIntent()

    return Notification.Action.Builder(
        Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
        getString(R.string.open),
        openIntent
    ).build()
}

private fun Service.buildStopAction(): Notification.Action {
    val stopIntent = NavDeepLinkBuilder(this)
        .setGraph(R.navigation.nav_run)
        .setDestination(R.id.stopFragment)
        .setComponentName(RunActivity::class.java)
        .createPendingIntent()

    return Notification.Action.Builder(
        Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
        getString(R.string.stop),
        stopIntent
    ).build()
}
