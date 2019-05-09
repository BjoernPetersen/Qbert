package net.bjoernpetersen.qbert.android

import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager

enum class ActivePluginType(val type: PluginType) {
    GENERIC(PluginType.GENERIC),
    PROVIDER(PluginType.PROVIDER),
    SUGGESTER(PluginType.SUGGESTER);

    fun select(dependencyManager: DependencyManager): List<Plugin> {
        return type.select(dependencyManager)
    }
}
