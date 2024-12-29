package io.github.kroune

import dev.hayden.KHealth
import io.github.kroune.logger.identifier
import io.github.kroune.logger.openTelemetryLogger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.instrumentation.ktor.v3_0.server.KtorServerTracing
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.export.RetryPolicy
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes
import org.slf4j.event.Level
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun Application.configureMonitoring() {
    install(KHealth)
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    val openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(
                    BatchSpanProcessor.builder(
                        OtlpGrpcSpanExporter.builder()
                            .setEndpoint(ConfigurationLoader.currentConfig.serviceLocator.otlp.url)
                            .setCompression("gzip")
                            .setRetryPolicy(RetryPolicy.getDefault())
                            .build()
                    )
                        .setExporterTimeout(3.seconds.toJavaDuration())
                        .build()
                )
                .setResource(
                    Resource.builder()
                        .put(ServiceAttributes.SERVICE_NAME, "nine-mens-morris-artifact")
                        .put(ServiceAttributes.SERVICE_VERSION, identifier)
                        .build()
                )
                .build()
        )
        .setLoggerProvider(
            openTelemetryLogger
        )
        .build()
    install(KtorServerTracing) {
        setOpenTelemetry(openTelemetry)

        knownMethods(HttpMethod.DefaultMethods)
        capturedRequestHeaders(HttpHeaders.UserAgent)
        capturedResponseHeaders(HttpHeaders.ContentType)

        spanStatusExtractor {
            if (error != null) {
                spanStatusBuilder.setStatus(StatusCode.ERROR)
            }
        }

        spanKindExtractor {
            if (httpMethod == HttpMethod.Post) {
                SpanKind.PRODUCER
            } else {
                SpanKind.CLIENT
            }
        }

        attributeExtractor {
            onStart {
                attributes.put("start-time", Instant.now().toEpochMilli())
            }
            onEnd {
                attributes.put("end-time", Instant.now().toEpochMilli())
            }
        }
    }
}
