package net.bjoernpetersen.qbert.impl

import net.bjoernpetersen.musicbot.api.plugin.PluginLoader
import net.bjoernpetersen.musicbot.spi.plugin.GenericPlugin
import net.bjoernpetersen.musicbot.spi.plugin.PlaybackFactory
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import net.bjoernpetersen.spotify.auth.SpotifyAuthenticatorImpl
import net.bjoernpetersen.spotify.control.SpotifyControlImpl
import net.bjoernpetersen.spotify.playback.SpotifyPlaybackFactory
import net.bjoernpetersen.spotify.provider.SpotifyProviderImpl
import net.bjoernpetersen.spotify.suggester.PlaylistSuggester
import net.bjoernpetersen.spotify.suggester.RecommendationSuggester
import net.bjoernpetersen.spotify.suggester.SavedTracksSuggester
import net.bjoernpetersen.spotify.suggester.SongRepeatSuggester
import net.bjoernpetersen.spotify.volume.SpotifyVolumeHandler
import kotlin.reflect.KClass

class BundlePluginLoader : PluginLoader {
    override val loader = this::class.java.classLoader
    @Suppress("UNCHECKED_CAST")
    override fun <T : Plugin> load(type: KClass<T>): Collection<T> {
        val classes: List<KClass<*>> = when (type) {
            GenericPlugin::class -> genericPlugins
            PlaybackFactory::class -> playbackFactories
            Provider::class -> providers
            Suggester::class -> suggesters
            else -> emptyList()
        }
        return classes.map { it.java.newInstance() as T }
    }

    private companion object {
        val genericPlugins = listOf(
            SpotifyAuthenticatorImpl::class,
            SpotifyControlImpl::class,
            SpotifyVolumeHandler::class
        )

        val playbackFactories = listOf(
            SpotifyPlaybackFactory::class
        )

        val providers = listOf(
            SpotifyProviderImpl::class
        )

        val suggesters = listOf<KClass<out Suggester>>(
            PlaylistSuggester::class,
            RecommendationSuggester::class,
            SavedTracksSuggester::class,
            SongRepeatSuggester::class
        )
    }
}
