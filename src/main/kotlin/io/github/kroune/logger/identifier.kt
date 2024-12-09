package io.github.kroune.logger

import io.opentelemetry.api.logs.Logger
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import java.util.*

val openTelemetryEndpoint: String = run {
    val isInK8s = System.getenv("IS_IN_K8S") == "1"
    val localhost = "http://127.0.0.1:4317"
    // we need to specify port
    val podDomain = "http://my-release-signoz-otel-collector.platform.svc.cluster.local:4317"
    val endpoint = if (isInK8s) podDomain else localhost
    endpoint
}
val identifier = UUID.randomUUID().toString()
val openTelemetryLogger: SdkLoggerProvider = SdkLoggerProvider.builder()
    .addLogRecordProcessor(
        BatchLogRecordProcessor.builder(
            OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(openTelemetryEndpoint)
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