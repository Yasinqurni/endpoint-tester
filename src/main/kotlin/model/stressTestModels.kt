package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// Stress Testing Models
@Serializable
data class StressTestConfig(
    @SerialName("endpoint") val endpointUrl: String,
    @SerialName("method") val httpMethod: String = "GET",
    @SerialName("body") val requestBody: String? = null,
    @SerialName("formData") val formData: Map<String, String>? = null,
    @SerialName("fileData") val fileData: Map<String, String>? = null,
    @SerialName("concurrentUsers") val concurrentUsers: Int = 10,
    @SerialName("totalRequests") val totalRequests: Int = 100,
    @SerialName("rampUpTime") val rampUpTimeSeconds: Int = 10,
    @SerialName("duration") val durationSeconds: Int = 60
)

@Serializable
data class StressTestRequest(
    @SerialName("headers") val headers: Headers,
    val config: StressTestConfig
)

@Serializable
data class StressTestMetrics(
    val totalRequests: Int,
    val successfulRequests: Int,
    val failedRequests: Int,
    val averageResponseTime: Double,
    val minResponseTime: Long,
    val maxResponseTime: Long,
    val requestsPerSecond: Double,
    val errorRate: Double,
    val duration: Long
)

@Serializable
data class StressTestResult(
    val config: StressTestConfig,
    val metrics: StressTestMetrics,
    val results: List<EndpointCallResult>,
    val message: String
)

@Serializable
data class StressTestLogData(
    val timestamp: String,
    val config: StressTestConfig,
    val metrics: StressTestMetrics,
    val totalResults: Int,
    val successRate: String,
    val avgResponseTime: String,
    val requestsPerSecond: Double
)


