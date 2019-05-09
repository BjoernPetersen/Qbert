package net.bjoernpetersen.qbert.android

import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.plugin.GenericPlugin
import net.bjoernpetersen.musicbot.spi.plugin.PlaybackFactory
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

enum class PluginType(val type: KClass<out Plugin>) {
    GENERIC(GenericPlugin::class) {
        override fun select(dependencyManager: DependencyManager): List<Plugin> {
            return dependencyManager.genericPlugins
        }

        override fun select(finder: PluginFinder): List<Plugin> {
            return finder.genericPlugins
        }
    },
    PLAYBACK_FACTORY(PlaybackFactory::class) {
        override fun select(dependencyManager: DependencyManager): List<Plugin> {
            return dependencyManager.playbackFactories
        }

        override fun select(finder: PluginFinder): List<Plugin> {
            return finder.playbackFactories
        }
    },
    PROVIDER(Provider::class) {
        override fun select(dependencyManager: DependencyManager): List<Plugin> {
            return dependencyManager.providers
        }

        override fun select(finder: PluginFinder): List<Plugin> {
            return finder.providers
        }
    },
    SUGGESTER(Suggester::class) {
        override fun select(dependencyManager: DependencyManager): List<Plugin> {
            return dependencyManager.suggesters
        }

        override fun select(finder: PluginFinder): List<Plugin> {
            return finder.suggesters
        }
    };

    val displayName = type.simpleName!!

    abstract fun select(dependencyManager: DependencyManager): List<Plugin>
    abstract fun select(finder: PluginFinder): List<Plugin>
    override fun toString(): String = displayName
}

val KClass<*>.type: PluginType
    get() {
        return PluginType.values()
            .first { it.type.isSuperclassOf(this) }
    }
