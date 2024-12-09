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
        post("/upload") {
            val parameters = call.receiveParameters()
            val authToken = parameters["auth_token"]
            val type = parameters["type"]
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
            if (commit == null || branch == null || type == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val file = call.receive<ByteArray>()
            artifactsRepository.uploadArtifact(
                artifactValue = file,
                branchValue = branch,
                typeValue = type,
                commitValue = commit
            )
            call.respond(HttpStatusCode.OK)
            return@post
        }
        get("/artifact") {
            val type = call.parameters["type"]
            val commit = call.parameters["commit"]
            val branch = call.parameters["branch"]
            val artifact = artifactsRepository.getArtifact(branchValue = branch, typeValue = type, commitValue = commit)
            if (artifact == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respondBytes(artifact)
            call.respond(HttpStatusCode.OK)
            log.info("provided an artifact = $type, branch = $branch, commit = $commit")
            return@get
        }
        get("upload") {
            val file = this.javaClass.getResourceAsStream("/upload/index.html").readAllBytes()
            call.respondBytes(file)
//            call.respondHtml(HttpStatusCode.OK) {
//                head {
//                    title {
//                        +"Upload artifacts manually"
//                    }
//                }
//                body {
//                    form(
//                        action = "/upload-manually",
//                        method = FormMethod.get
//                    ) {
//                        p {
//                            +"Secret token"
//                            textInput(
//                                name = "auth_token"
//                            ) {
//                            }
//                        }
//                        p {
//                            +"branch"
//                            textInput(
//                                name = "branch"
//                            ) {
//                            }
//                        }
//                        p {
//                            +"commit"
//                            textInput(
//                                name = "commit"
//                            ) {
//                            }
//                        }
//                        p {
//                            +"type"
//                            textInput(
//                                name = "type"
//                            ) {
//                            }
//                        }
//                        p {
//                            +"file"
//                            fileInput (
//                                name = "type"
//                            ) {
//                            }
//                        }
//                        p {
//                            submitInput { value = "Upload" }
//                        }
//                    }
//                }
//            }
        }
        // Static plugin. Try to access `/static/index.html`
        staticResources("/", "static")
    }
}