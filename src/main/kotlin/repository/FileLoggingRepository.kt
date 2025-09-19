package repository

import model.EndpointCallItem
import model.EndpointCallResult
import model.StressTestConfig
import model.StressTestMetrics
import model.StressTestLogData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.OffsetDateTime

interface LoggingRepository {
    suspend fun writeRequest(item: EndpointCallItem, baseUrl: String? = null)
    suspend fun writeResult(result: EndpointCallResult)
    suspend fun writeStressTestResult(config: StressTestConfig, metrics: StressTestMetrics, results: List<EndpointCallResult>)
}

class LoggingRepositoryImpl(
    private val logDir: Path,
    private val json: Json = Json { prettyPrint = true }
) : LoggingRepository {

    init {
        if (!Files.exists(logDir)) {
            Files.createDirectories(logDir)
        }
    }

    override suspend fun writeRequest(item: EndpointCallItem, baseUrl: String? = null) {
        val line = buildString {
            append(OffsetDateTime.now().toString())
            append(" | REQUEST | ")
            append("Title: ${item.title} | ")
            baseUrl?.let { append("BaseURL: $it | ") }
            append("Method: ${item.httpMethod} | ")
            append("Endpoint: ${item.endpointUrl} | ")
            append(json.encodeToString(item))
            append('\n')
        }
        Files.write(logFilePath(), line.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    override suspend fun writeResult(result: EndpointCallResult) {
        val line = buildString {
            append(OffsetDateTime.now().toString())
            append(" | RESPONSE | ")
            append("Title: ${result.title} | ")
            append("Status: ${result.statusCode} | ")
            append("Success: ${result.success} | ")
            append("Duration: ${result.durationMs}ms | ")
            append(json.encodeToString(result))
            append('\n')
        }
        Files.write(logFilePath(), line.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    override suspend fun writeStressTestResult(config: StressTestConfig, metrics: StressTestMetrics, results: List<EndpointCallResult>) {
        val stressTestData = StressTestLogData(
            timestamp = OffsetDateTime.now().toString(),
            config = config,
            metrics = metrics,
            totalResults = results.size,
            successRate = "${metrics.successfulRequests}/${metrics.totalRequests}",
            avgResponseTime = "${metrics.averageResponseTime}ms",
            requestsPerSecond = metrics.requestsPerSecond
        )
        
        val line = buildString {
            append(OffsetDateTime.now().toString())
            append(" | STRESS_TEST | ")
            append(json.encodeToString(stressTestData))
            append('\n')
        }
        
        Files.write(stressTestLogFilePath(), line.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    private fun logFilePath(): Path = logDir.resolve("endpoint-tester.log")
    private fun stressTestLogFilePath(): Path = logDir.resolve("stress-test.log")
}


