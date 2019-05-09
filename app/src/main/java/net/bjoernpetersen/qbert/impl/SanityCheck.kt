package net.bjoernpetersen.qbert.impl

import androidx.annotation.StringRes
import net.bjoernpetersen.musicbot.api.plugin.management.findDependencies
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager
import net.bjoernpetersen.musicbot.spi.util.BrowserOpener
import net.bjoernpetersen.qbert.R
import net.bjoernpetersen.qbert.android.ActivePluginType
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import java.io.File
import java.net.URL
import kotlin.reflect.KClass

fun Lifecyclist.checkSanity(filesDir: File): List<Insanity> {
    val manager = create(filesDir)
    if (manager.providers.none { manager.isEnabled(it) }) return listOf(NoProvider)

    val unsatisfied = ActivePluginType.values()
        .flatMap { it.select(manager) }
        .filter { manager.isEnabled(it) }
        .flatMap { it.findDependencies() }
        .distinct()
        .mapNotNull { manager.checkSatisfaction(it) }
        .distinct()
        .map { MissingDependency(it.qualifiedName!!) }
    if (unsatisfied.isNotEmpty()) return unsatisfied

    inject(DummyBrowserOpener)

    val finder = getPluginFinder()
    return finder.allPlugins()
        .mapNotNull { plugin ->
            val configs = getConfigs(plugin)
            val tab = if (configs.plain.any { it.checkError() != null }) 0
            else if (configs.secret.any { it.checkError() != null }) 1
            else return@mapNotNull null
            PluginConfigError(plugin::class.qualifiedName!!, tab)
        }
        .toList()
}

sealed class Insanity(@StringRes val errorMessage: Int)
object NoProvider : Insanity(R.string.no_provider)
class PluginConfigError(
    val pluginClassName: String,
    val tab: Int
) : Insanity(R.string.config_errors)

class MissingDependency(val baseClassName: String) : Insanity(R.string.dependency_errors)

fun DependencyManager.checkSatisfaction(
    base: KClass<out Plugin>,
    visited: MutableSet<String> = HashSet(64)
): KClass<out Plugin>? {
    if (!visited.add(base.qualifiedName!!)) {
        return null
    }

    val default = getDefault(base) ?: return base
    return default.findDependencies()
        .asSequence()
        .mapNotNull { checkSatisfaction(it, visited) }
        .firstOrNull()
}

private object DummyBrowserOpener : BrowserOpener {
    override fun openDocument(url: URL) = Unit

}
