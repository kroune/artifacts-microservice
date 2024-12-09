package io.github.kroune

import io.github.kroune.local.artifactsRepository
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database

val globalAuthToken = System.getenv("auth_token")!!.also { value ->
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

fun Application.module() {
    // check if it is valid
    globalAuthToken
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
    artifactsRepository
    configureMonitoring()
    configureRouting()
}