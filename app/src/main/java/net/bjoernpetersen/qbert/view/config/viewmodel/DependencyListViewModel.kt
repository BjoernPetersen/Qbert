package net.bjoernpetersen.qbert.view.config.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.bjoernpetersen.musicbot.api.plugin.management.findDependencies
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.idName
import net.bjoernpetersen.musicbot.spi.plugin.isIdBase
import net.bjoernpetersen.qbert.impl.checkSatisfaction
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import net.bjoernpetersen.qbert.view.config.adapter.PluginInfo
import java.io.File
import kotlin.reflect.KClass

class DependencyListViewModel : ViewModel() {
    val info = MutableLiveData(emptyList<PluginInfo>())

    fun refreshInfo(filesDir: File, bases: List<String>) {
        viewModelScope.launch(Dispatchers.Default) {
            @Suppress("UNCHECKED_CAST")
            val baseClasses = bases.map { Class.forName(it).kotlin as KClass<out Plugin> }
            val life = Lifecyclist()
            val manager = life.create(filesDir)
            val result = baseClasses
                .map {
                    val displayName = if (it.isIdBase) it.idName else it.simpleName!!
                    val active = manager.getDefault(it)
                    val dependencies = active?.findDependencies()
                    val hasDependencies = dependencies?.isNotEmpty() ?: false
                    val satisfied = dependencies
                        ?.all { dep -> manager.checkSatisfaction(dep) == null }
                        ?: false
                    PluginInfo(
                        it.qualifiedName!!,
                        displayName,
                        active,
                        hasDependencies = hasDependencies,
                        isSatisfied = satisfied
                    )
                }
            info.postValue(result)
        }
    }

    suspend fun getDependencies(implClassName: String): List<String> {
        return withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            val baseClass = Class.forName(implClassName).kotlin
            baseClass.findDependencies().map { it.qualifiedName!! }
        }
    }
}
