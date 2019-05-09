package net.bjoernpetersen.qbert.view.config.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import java.io.File
import kotlin.reflect.KClass

class ImplementationSelectViewModel(
    private val base: KClass<out Plugin>,
    private val filesDir: File
) : ViewModel() {
    val implementations = MutableLiveData(emptyList<Plugin>())
    val default = MutableLiveData<Plugin>(null)

    suspend fun refreshImplementations() {
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            val manager = getManager()
            implementations.postValue(manager.findAvailable(base))
            default.postValue(manager.getDefault(base))
        }
    }

    private fun getManager(): DependencyManager {
        return Lifecyclist().create(filesDir)
    }

    fun setDefault(plugin: Plugin?) {
        viewModelScope.launch(Dispatchers.Default) {
            getManager().setDefault(plugin, base)
            default.postValue(plugin)
        }
    }

    class Factory(
        private val base: KClass<out Plugin>,
        private val filesDir: File
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ImplementationSelectViewModel(base, filesDir) as T
        }
    }
}
