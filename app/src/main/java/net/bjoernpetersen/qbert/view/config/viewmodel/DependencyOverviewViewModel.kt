package net.bjoernpetersen.qbert.view.config.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.bjoernpetersen.musicbot.spi.plugin.bases
import net.bjoernpetersen.musicbot.spi.plugin.isIdBase
import net.bjoernpetersen.qbert.android.ActivePluginType
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import java.io.File
import java.util.EnumMap
import java.util.LinkedList
import kotlin.reflect.full.isSuperclassOf

class DependencyOverviewViewModel : ViewModel() {
    val bases = MutableLiveData(emptyMap<ActivePluginType, List<String>>())

    fun refreshActiveBases(filesDir: File) {
        viewModelScope.launch(Dispatchers.Default) {
            val manager = Lifecyclist().create(filesDir)
            val found = manager.allPlugins
                .flatMap { it.bases.asSequence() }
                .filter { it.isIdBase }
                .distinct()
                .groupByTo(
                    EnumMap<ActivePluginType, MutableList<String>>(ActivePluginType::class.java)
                        .apply { fill() },
                    { base ->
                        ActivePluginType.values()
                            .first { it.type.type.isSuperclassOf(base) }
                    },
                    { it.qualifiedName!! }
                )

            bases.postValue(found)
        }
    }

    @Suppress("ReplacePutWithAssignment")
    private fun EnumMap<ActivePluginType, MutableList<String>>.fill() {
        ActivePluginType.values().forEach { this.put(it, LinkedList()) }
    }
}
