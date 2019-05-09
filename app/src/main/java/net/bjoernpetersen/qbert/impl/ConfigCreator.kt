package net.bjoernpetersen.qbert.impl

import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.config.PluginConfigScope
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist

fun Lifecyclist.getConfigs(plugin: Plugin): ConfigEntries {
    require(stage == Lifecyclist.Stage.Injected)
    val scope = PluginConfigScope(plugin::class)
    val configs = getConfigManager()[scope]
    val plain = plugin.createConfigEntries(configs.plain)
    val secret = plugin.createSecretEntries(configs.secrets)
    return ConfigEntries(plain, secret)
}

class ConfigEntries(
    val plain: List<Config.Entry<*>>,
    val secret: List<Config.Entry<*>>
)
