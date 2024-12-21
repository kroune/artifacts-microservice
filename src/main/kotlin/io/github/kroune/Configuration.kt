package io.github.kroune

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.io.File
import kotlin.time.Duration

object ConfigurationLoader {
    @Serializable
    class ConfigMember(
        val server: Server,
        val rateLimit: RateLimit,
        val authToken: String,
        val serviceLocator: ServiceLocator
    ) {
        init {
            require(
                authToken.any { it.isDigit() }
                        && authToken.any { it.isLetter() }
                        && authToken.all { it.isLetterOrDigit() }
                        && authToken.length >= 20
            )
        }
    }

    @Serializable
    class Server(
        val host: String,
        val port: Int
    )

    @Serializable
    class RateLimit(
        val limit: Int,
        val refillSpeed: Duration,
        val initialSize: Int
    )

    @Serializable
    class ServiceLocator(
        val postgres: PostgresConfig
    )

    @Serializable
    class PostgresConfig(
        val url: String,
        val user: String,
        val password: String
    )

    private fun loadConfig(): ConfigMember {
        val configDirectory = System.getenv("CONFIG_PATH") ?: "/etc/artifacts-server/config.yaml"
        val config = File(configDirectory).readText()
        return Yaml.default.decodeFromString<ConfigMember>(config)
    }

    val currentConfig: ConfigMember = loadConfig()
}