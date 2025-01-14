package io.github.kroune

import io.github.kroune.local.artifactsRepository
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.reflect.full.createInstance

class TestDatabase {
    private var mySQLContainer: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:17").apply {
        withDatabaseName("test-db")
        withUsername("test-user")
        withPassword("test-password")
        start() // Start the container
    }

    suspend fun addDummyArtifacts(publishType: PublishType = PublishType.Testing, branch: String = "main") {
        artifactsRepository.uploadArtifact(
            this.javaClass.getResource("/artifacts/nineMensMorris-1.0.0-android.apk")!!.readBytes(),
            branch,
            publishType,
            "commitId",
            PlatformType.Android
        )
        artifactsRepository.uploadArtifact(
            this.javaClass.getResource("/artifacts/nineMensMorris-1.0.0-linux.deb")!!.readBytes(),
            branch,
            publishType,
            "commitId",
            PlatformType.LinuxDeb
        )
        artifactsRepository.uploadArtifact(
            this.javaClass.getResource("/artifacts/nineMensMorris-1.0.0-macos.dmg")!!.readBytes(),
            branch,
            publishType,
            "commitId",
            PlatformType.MacosDmg
        )
        artifactsRepository.uploadArtifact(
            this.javaClass.getResource("/artifacts/nineMensMorris-1.0.0-web.zip")!!.readBytes(),
            branch,
            publishType,
            "commitId",
            PlatformType.Web
        )
        artifactsRepository.uploadArtifact(
            this.javaClass.getResource("/artifacts/nineMensMorris-1.0.0-windows.exe")!!.readBytes(),
            branch,
            publishType,
            "commitId",
            PlatformType.WindowsExe
        )
    }

    fun connect() {
        Database.connect(
            mySQLContainer.jdbcUrl,
            driver = "org.postgresql.Driver",
            user = mySQLContainer.username,
            password = mySQLContainer.password
        )

        // beautiful, isn't it?
        println("creating tables")
        artifactsRepository = artifactsRepository::class.createInstance()
        println("created tables")
    }
}