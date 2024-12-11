package io.github.kroune

import io.github.kroune.local.artifactsRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.Database
import kotlin.time.Duration.Companion.seconds

val globalAuthToken = System.getenv("auth_token")!!.trim().also { value ->
    require(
        value.any { it.isDigit() }
                && value.any { it.isLetter() }
                && value.all { it.isLetterOrDigit() }
                && value.length >= 20
    )
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(connectToDb: Boolean = true) {
    // check if it is valid
    globalAuthToken
    if (connectToDb) {
        val isInK8s = System.getenv("IS_IN_K8S") == "1"
        val localhost = "127.0.0.1:5432"
        val podDomain = "postgres-service.default.svc.cluster.local"
        val url = if (isInK8s) podDomain else localhost
        Database.connect(
            "jdbc:postgresql://$url/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "1234"
        )
    }
    install(RateLimit) {
        global {
            rateLimiter(10, 10.seconds, 5)
        }
    }
    install(StatusPages) {
        unhandled { call ->
            call.respondRedirect("/artifacts-service/")
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    artifactsRepository
    configureMonitoring()
    configureRouting()
}
