package net.bjoernpetersen.qbert.view.run.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import net.bjoernpetersen.musicbot.api.player.PlayerState
import net.bjoernpetersen.musicbot.api.player.ProgressTracker
import net.bjoernpetersen.musicbot.api.player.QueueEntry
import net.bjoernpetersen.musicbot.spi.player.Player
import net.bjoernpetersen.musicbot.spi.player.QueueChangeListener
import net.bjoernpetersen.musicbot.spi.player.SongQueue
import net.bjoernpetersen.qbert.QbertApp
import net.bjoernpetersen.qbert.R
import net.bjoernpetersen.qbert.impl.createBrowserOpener
import net.bjoernpetersen.qbert.impl.toDurationString
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import java.time.Duration

class RunViewModel(app: Application) : AndroidViewModel(app) {
    private val app: QbertApp
        get() = getApplication()
    private val life = Lifecyclist()
    private val mutPlayerState = MutableLiveData<PlayerState>(null)
    val playerState: LiveData<PlayerState>
        get() = mutPlayerState
    private val mutQueue = MutableLiveData<List<QueueEntry>>(null)
    val queue: LiveData<List<QueueEntry>>
        get() = mutQueue
    private val mutProgress = MutableLiveData<String>(null)
    val progress: LiveData<String>
        get() = mutProgress

    private val loadMutex = Mutex(true)
    private lateinit var player: Player

    init {
        viewModelScope.launch(Dispatchers.Default) {
            life.create(app.filesDir)
            life.inject(app.createBrowserOpener())

            TODO("")
            // TODO do this somewhere else
            //life.run { it?.printStackTrace() }

            player = life.getInjector().getInstance(Player::class.java)
            loadMutex.unlock()
            val unknown = app.resources.getString(R.string.unknown)
            val durationTemplate = app.resources.getString(R.string.duration_template)
            var songDuration: String = unknown

            fun updatePlayerState(state: PlayerState) {
                mutPlayerState.postValue(state)
                songDuration = state.entry?.song?.duration?.toDurationString() ?: unknown
            }
            player.addListener { old, new -> if (old != new) updatePlayerState(new) }
            updatePlayerState(player.state)

            val queue = life.getInjector().getInstance(SongQueue::class.java)
            queue.addListener(object : QueueChangeListener {
                override fun onAdd(entry: QueueEntry) {
                    mutQueue.postValue(queue.toList())
                }

                override fun onRemove(entry: QueueEntry) {
                    mutQueue.postValue(queue.toList())
                }

                override fun onMove(entry: QueueEntry, fromIndex: Int, toIndex: Int) {
                    mutQueue.postValue(queue.toList())
                }
            })

            val progressTracker = life.getInjector().getInstance(ProgressTracker::class.java)
            launch {
                while (true) {
                    val progress = progressTracker.getCurrentProgress()
                    val progressString = progress.duration.seconds.toInt().toDurationString()
                    val durationString = durationTemplate.format(progressString, songDuration)
                    mutProgress.postValue(durationString)
                    delay(Duration.ofMillis(50))
                }
            }
        }
    }

    override fun onCleared() {
        GlobalScope.launch {
            loadMutex.withLock {
                life.stop()
            }
        }
    }

    suspend fun play() {
        withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            loadMutex.withLock { }
            player.play()
        }
    }

    suspend fun pause() {
        withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            loadMutex.withLock { }
            player.pause()
        }
    }

    suspend fun next() {
        withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            loadMutex.withLock { }
            player.next()
        }
    }
}
