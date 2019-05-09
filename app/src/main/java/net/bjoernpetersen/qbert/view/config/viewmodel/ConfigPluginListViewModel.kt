package net.bjoernpetersen.qbert.view.config.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.util.BrowserOpener
import net.bjoernpetersen.qbert.QbertApp
import net.bjoernpetersen.qbert.android.PluginType
import net.bjoernpetersen.qbert.impl.ConfigEntries
import net.bjoernpetersen.qbert.impl.getConfigs
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ConfigPluginListViewModel(
    app: Application
) : AndroidViewModel(app) {
    private val lifeLock: Lock = ReentrantLock()
    private val life = Lifecyclist()
    private val app: QbertApp
        get() = getApplication()

    private fun getLife(browserOpener: BrowserOpener): Lifecyclist {
        if (life.stage < Lifecyclist.Stage.Injected) lifeLock.withLock {
            if (life.stage < Lifecyclist.Stage.Injected) {
                life.create(app.filesDir)
                life.inject(browserOpener)
            }
        }
        return life
    }

    suspend fun findPlugins(
        browserOpener: BrowserOpener,
        type: PluginType
    ): List<Plugin> {
        return withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            val finder = getLife(browserOpener).getPluginFinder()
            type.select(finder)
        }
    }

    suspend fun getPlugin(
        browserOpener: BrowserOpener,
        pluginClassName: String
    ): Plugin {
        return withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            val finder = getLife(browserOpener).getPluginFinder()
            finder.allPlugins().first { it::class.qualifiedName == pluginClassName }
        }
    }

    suspend fun getMainConfigEntries(
        browserOpener: BrowserOpener
    ): List<Config.Entry<*>> {
        return withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            getLife(browserOpener).getMainConfig().allPlain
        }
    }

    suspend fun findConfigs(
        browserOpener: BrowserOpener,
        plugin: Plugin
    ): ConfigEntries {
        return withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            getLife(browserOpener).getConfigs(plugin)
        }
    }
}
