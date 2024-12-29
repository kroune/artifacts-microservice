package io.github.kroune.logger

import io.github.kroune.ConfigurationLoader
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

val identifier = UUID.randomUUID().toString()
val openTelemetryLogger: SdkLoggerProvider = SdkLoggerProvider.builder()
    .addLogRecordProcessor(
        BatchLogRecordProcessor.builder(
            OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(ConfigurationLoader.currentConfig.serviceLocator.otlp.url)
                .setCompression("gzip")
                .build()
        )
            .build()
    )
    .setResource(
        Resource.builder()
            .put(ServiceAttributes.SERVICE_NAME, "nine-mens-morris-artifact")
            .put(ServiceAttributes.SERVICE_VERSION, identifier)
            .build()
    )
    .build()

val loggerInstance: Logger = openTelemetryLogger
    .loggerBuilder("nine-mens-morris-artifact")
    .build()

fun log(
    text: String,
    severity: Severity,
    throwable: Throwable? = null
) {
    val sdf = SimpleDateFormat("hh:mm:ss dd/M/yyyy ")
    val currentDate = sdf.format(Date())
    println("$currentDate $text ${throwable?.stackTraceToString() ?: ""}")
    loggerInstance.logRecordBuilder()
        .setBody(text)
        .apply {
            if (throwable != null) {
                this.setAttribute(AttributeKey.stringKey("exception"), throwable.message ?: "empty message")
                this.setAttribute(AttributeKey.stringKey("stackTrace"), throwable.stackTraceToString())
            }
        }
        .setTimestamp(Instant.now())
        .setSeverity(severity)
        .emit()
}