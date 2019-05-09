package net.bjoernpetersen.qbert.rest.location

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import net.bjoernpetersen.qbert.rest.model.ImplementationInfo
import net.bjoernpetersen.qbert.rest.model.VersionInfo

private const val PROJECT_PAGE = "https://github.com/BjoernPetersen/Qbert"
private const val PROJECT_NAME = "Qbert"

@KtorExperimentalLocationsAPI
@Location("/version")
class Version {
    companion object {
        val versionInfo: VersionInfo by lazy { loadInfo() }

        private fun loadInfo(): VersionInfo {
            val implVersion = loadImplementationVersion()
            val apiVersion = loadApiVersion()
            return VersionInfo(
                apiVersion,
                ImplementationInfo(
                    PROJECT_PAGE,
                    PROJECT_NAME,
                    implVersion
                )
            )
        }

        // TODO load from PackageManager
        private fun loadImplementationVersion(): String = "0.1.0"

        private fun loadApiVersion(): String = "0.13.0"
    }
}
