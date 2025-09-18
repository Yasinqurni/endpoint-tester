package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Headers(
    @SerialName("Authorization") val Authorization: String? = null,
    @SerialName("Content-Type") val ContentType: String? = null,
    @SerialName("Accept") val Accept: String? = null,
    @SerialName("User-Agent") val UserAgent: String? = null,
    @SerialName("Accept-Language") val AcceptLanguage: String? = null,
    @SerialName("Accept-Encoding") val AcceptEncoding: String? = null,
    @SerialName("Accept-Charset") val AcceptCharset: String? = null,
)


@Serializable
data class EndpointCallItem(
    @SerialName("endpoint") val endpointUrl: String,
    @SerialName("method") val httpMethod: String,
    @SerialName("body") val requestBody: String? = null,
    @SerialName("formData") val formData: Map<String, String>? = null,  // Form data fields
    @SerialName("fileData") val fileData: Map<String, String>? = null,  // File data (filename -> content)
)

@Serializable
data class EndpointTesterRequest(
    @SerialName("headers") val headers: Headers,
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
