package io.github.kroune

import io.github.kroune.ConfigurationLoader.currentConfig
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
                val parameters = call.parameters
                val authToken = parameters["auth_token"]
                val type = parameters["type"]
                val platform = parameters["platform"]
                val commit = parameters["commit"]
                val branch = parameters["branch"]

                if (authToken != currentConfig.authToken) {
                    call.respond(HttpStatusCode.Unauthorized, "[auth_token] is invalid")
                    return@post
                }
                if (commit == null || branch == null || type == null || platform == null) {
                    call.respond(HttpStatusCode.BadRequest, "one of query parameters is null")
                    return@post
                }
                val resolvedPlatform = platform.decodeToPlatformType()
                if (resolvedPlatform == null) {
                    call.respond(HttpStatusCode.BadRequest, "[platform] type wasn't resolved")
                    return@post
                }
                val resolvedType = type.decodeToPublishType()
                if (resolvedType == null) {
                    call.respond(HttpStatusCode.BadRequest, "[type] is invalid")
                    return@post
                }
                val file = call.receive<ByteArray>()
                if (file.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "content is empty")
                    return@post
                }
                artifactsRepository.uploadArtifact(
                    artifactValue = file,
                    branchValue = branch,
                    typeValue = resolvedType,
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
                    call.respond(HttpStatusCode.BadRequest, "[platform] type wasn't resolved")
                    return@get
                }
                val resolvedType = type?.decodeToPublishType() ?: PublishType.Release
                val artifact =
                    artifactsRepository.getArtifact(
                        branchValue = branch,
                        typeValue = resolvedType,
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
            staticResources("/", "/static")
        }
    }
}
