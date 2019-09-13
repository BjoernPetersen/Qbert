@file:Suppress("MatchingDeclarationName")

package net.bjoernpetersen.qbert.rest.location

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.routing.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.qbert.rest.require
import net.bjoernpetersen.qbert.rest.respondEmpty

private val logger = KotlinLogging.logger {}

@KtorExperimentalLocationsAPI
@Location("/exit")
class ExitRequest

private const val GRACE_PERIOD_MILLIS = 500L

@KtorExperimentalLocationsAPI
fun Route.routeExit() {
    authenticate {
        post<ExitRequest> {
            require(Permission.EXIT)
            call.respondEmpty()
            GlobalScope.launch(Dispatchers.Main) {
                delay(GRACE_PERIOD_MILLIS)
                logger.info { "Closing due to remote user request" }
                // FIXME: exit
            }
        }
    }
}
