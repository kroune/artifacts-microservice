package io.github.kroune

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        TestDatabase().connect()
        application {
            module(false)
        }
        client.get("/artifacts-service/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetArtifactMissingParameters() = testApplication {
        TestDatabase().apply {
            connect()
            addDummyArtifacts()
        }
        application {
            module(false)
        }

        client.get("/artifacts-service/artifact").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testGetArtifactsForAndroid() = testApplication {
        TestDatabase().apply {
            connect()
            addDummyArtifacts()
        }
        application {
            module(false)
        }

        client.get("/artifacts-service/artifact") {
            parameter("platform", "Android")
            parameters {
                append("platform", "Android")
            }
        }.apply {
            assertContentEquals(
                bodyAsBytes(),
                this.javaClass.getResource("/artifacts/nineMensMorris-1.0.0-android.apk")!!.readBytes()
            )
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
