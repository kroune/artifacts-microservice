package io.github.kroune

import io.github.kroune.ConfigurationLoader.currentConfig
import io.github.kroune.local.artifactsRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.Database

fun main() {
    // load configuration
    currentConfig
    embeddedServer(
        Netty,
        host = currentConfig.server.host,
        port = currentConfig.server.port,
        module = Application::module
    ).start(wait = true)
}

fun Application.module(connectToDb: Boolean = true) {
    // check if it is valid
    if (connectToDb) {
        currentConfig.serviceLocator.postgres.let { postges ->
            Database.connect(
                url = postges.url,
                driver = "org.postgresql.Driver",
                user = postges.user,
                password = postges.password
            )
        }
    }
    install(RateLimit) {
        global {
            currentConfig.rateLimit.let { rateLimit ->
                rateLimiter(
                    rateLimit.limit,
                    rateLimit.refillSpeed,
                    rateLimit.initialSize
                )
            }
        }
    }
    install(StatusPages) {
        unhandled { call ->
            call.respondRedirect("/artifacts-service/")
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    artifactsRepository
    configureMonitoring()
    configureRouting()
}
