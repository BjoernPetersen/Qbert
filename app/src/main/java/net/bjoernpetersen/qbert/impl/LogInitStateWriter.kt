package net.bjoernpetersen.qbert.impl

import mu.KotlinLogging
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.management.InitStateWriter

class LogInitStateWriter(private val plugin: Plugin) : InitStateWriter {
    private val logger = KotlinLogging.logger {}

    override fun state(state: String) {
        logger.info { "${plugin.name}: $state" }
    }

    override fun warning(warning: String) {
        logger.warn { "${plugin.name}: $warning" }
    }
}
