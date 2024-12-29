package io.github.kroune.logger

import io.github.kroune.ConfigurationLoader
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
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