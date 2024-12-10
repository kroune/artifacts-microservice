package io.github.kroune

import io.github.kroune.local.artifactsRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/artifacts-service") {
            post("/upload") {
                val parameters = call.receiveParameters()
                val authToken = parameters["auth_token"]
                val type = parameters["type"]
                val platform = parameters["platform"]
                val commit = parameters["commit"]
                val branch = parameters["branch"]

                if (authToken != globalAuthToken) {
                    println(authToken)
                    println(commit)
                    println(type)
                    println(branch)
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                if (commit == null || branch == null || type == null || platform == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                val resolvedPlatform = platform.decodeToPlatformType()
                if (resolvedPlatform == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                val file = call.receive<ByteArray>()
                artifactsRepository.uploadArtifact(
                    artifactValue = file,
                    branchValue = branch,
                    typeValue = type,
                    commitValue = commit,
                    platformValue = resolvedPlatform
                )
                call.respond(HttpStatusCode.OK)
                return@post
            }
            get("/artifact") {
                val type = call.parameters["type"]
                val commit = call.parameters["commit"]
                val branch = call.parameters["branch"]
                val platform = call.parameters["platform"]?.decodeToPlatformType()
                if (platform == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val artifact =
                    artifactsRepository.getArtifact(
                        branchValue = branch,
                        typeValue = type,
                        commitValue = commit,
                        platformType = platform
                    )
                if (artifact == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respondBytes(artifact)
                call.respond(HttpStatusCode.OK)
                log.info("provided an artifact = $type, branch = $branch, commit = $commit")
                return@get
            }
            // Static plugin. Try to access `/static/index.html`
            staticResources("/", "static")
        }
    }
}
