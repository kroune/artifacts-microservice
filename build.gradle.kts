plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.kroune"
version = "1.0.0"

application {
    mainClass.set("io.github.kroune.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven {
        url = uri("https://packages.confluent.io/maven")
        name = "confluence"
    }
}

tasks.withType<Test> {
    environment["auth_token"] = "someExample0TokenDontFuckingUseInProd1"
}

dependencies {
    implementation(libs.ktor.server.rate.limit)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.host.common.jvm)
    implementation(libs.ktor.server.call.logging.jvm)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.hayden.khealth)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlin.yaml)

    // db
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.exposed.json)
    implementation(libs.postgresql)

    // otlp
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.ktor)

    // testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.test.container.postgresql)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.test)
}
