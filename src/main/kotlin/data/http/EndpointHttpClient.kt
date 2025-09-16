package repository

import domain.model.EndpointCallItem
import domain.model.EndpointCallResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.system.measureTimeMillis

interface EndpointRepository {
    suspend fun execute(item: EndpointCallItem): EndpointCallResult
}

class KtorEndpointRepository(
    private val httpClient: HttpClient
) : EndpointRepository {

    override suspend fun execute(item: EndpointCallItem): EndpointCallResult {
        var status: Int = -1
        var responseBody: String? = null
        var responseHeaders: Map<String, String> = emptyMap()

        val duration = measureTimeMillis {
            try {
                val response = httpClient.request(item.endpointUrl) {
                    method = HttpMethod.parse(item.httpMethod)
                    if (!item.bearerToken.isNullOrBlank()) {
                        headers.append(HttpHeaders.Authorization, "Bearer ${item.bearerToken}")
                    }
                    item.headers?.forEach { (k, v) -> headers.append(k, v) }
                    if (!item.requestBody.isNullOrBlank()) {
                        setBody(item.requestBody)
                    }
                }
                status = response.status.value
                responseHeaders = response.headers.entries().associate { it.key to it.value.joinToString(",") }
                responseBody = response.bodyAsTextOrNullSafely()
            } catch (t: Throwable) {
                status = -1
                responseBody = t.message
                responseHeaders = emptyMap()
            }
        }

        return EndpointCallResult(
            endpoint = item.endpointUrl,
            method = item.httpMethod,
            statusCode = status,
            success = status in 200..299,
            durationMs = duration,
            responseHeaders = responseHeaders,
            responseBody = responseBody
        )
    }
}

private suspend fun io.ktor.client.statement.HttpResponse.bodyAsTextOrNullSafely(): String? =
    try {
        this.body<String>()
    } catch (_: Throwable) {
        null
    }


