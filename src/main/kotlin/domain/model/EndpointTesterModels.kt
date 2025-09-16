package domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EndpointCallItem(
    @SerialName("endpoint") val endpointUrl: String,
    @SerialName("method") val httpMethod: String,
    @SerialName("token") val bearerToken: String? = null,
    @SerialName("headers") val headers: Map<String, String>? = null,
    @SerialName("body") val requestBody: String? = null,
)

@Serializable
data class EndpointTesterRequest(
    val data: List<EndpointCallItem>
)

@Serializable
data class EndpointCallResult(
    val endpoint: String,
    val method: String,
    val statusCode: Int,
    val success: Boolean,
    val durationMs: Long,
    val responseHeaders: Map<String, String>,
    val responseBody: String?
)

@Serializable
data class EndpointTesterResponse(
    val results: List<EndpointCallResult>,
    val message: String
)


