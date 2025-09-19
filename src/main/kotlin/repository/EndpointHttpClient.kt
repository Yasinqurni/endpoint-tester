package repository

import model.EndpointCallItem
import model.EndpointCallResult
import model.Headers
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.system.measureTimeMillis
import java.io.File

interface EndpointRepository {
    suspend fun execute(item: EndpointCallItem, headers: Headers): EndpointCallResult
}

class EndpointRepositoryImpl (
    private val httpClient: HttpClient
) : EndpointRepository {

    override suspend fun execute(item: EndpointCallItem, headers: Headers): EndpointCallResult {
        var status: Int = -1
        var responseBody: String? = null
        var responseHeaders: Map<String, String> = emptyMap()

        val duration = measureTimeMillis {
            try {
                val endpoint = "${headers.BaseUrl.trimEnd('/')}/${item.endpointUrl.trimStart('/')}"
                val response = httpClient.request(endpoint) {
                    method = HttpMethod.parse(item.httpMethod)
                    
                    // Apply headers if provided
                    headers.let { headerObj ->
                        headerObj.Authorization?.let { 
                            this@request.headers.append(HttpHeaders.Authorization, it) 
                        }
                        headerObj.ContentType?.let { 
                            this@request.headers.append(HttpHeaders.ContentType, it) 
                        }
                        headerObj.Accept?.let { 
                            this@request.headers.append(HttpHeaders.Accept, it) 
                        }
                        headerObj.UserAgent?.let { 
                            this@request.headers.append(HttpHeaders.UserAgent, it) 
                        }
                        headerObj.AcceptLanguage?.let { 
                            this@request.headers.append(HttpHeaders.AcceptLanguage, it) 
                        }
                        headerObj.AcceptEncoding?.let { 
                            this@request.headers.append(HttpHeaders.AcceptEncoding, it) 
                        }
                        headerObj.AcceptCharset?.let { 
                            this@request.headers.append(HttpHeaders.AcceptCharset, it) 
                        }
                    }
                  
                    // Set body from various sources
                    when {
                        !item.requestBody.isNullOrBlank() -> {
                            setBody(item.requestBody)
                        }
                        item.requestBodyJson != null -> {
                            setBody(item.requestBodyJson.toString())
                        }
                        !item.formData.isNullOrEmpty() || !item.fileData.isNullOrEmpty() -> {
                            // Create multipart form data
                            val formDataContent = formData {
                                // Add regular form fields
                                item.formData?.forEach { (key, value) ->
                                    append(key, value)
                                }
                                
                                // Add file data
                                item.fileData?.forEach { (filename, content) ->
                                    append(filename, content, headersOf(
                                        HttpHeaders.ContentType to listOf("text/plain"),
                                        HttpHeaders.ContentDisposition to listOf("filename=\"$filename\"")
                                    ))
                                }
                            }
                            setBody(MultiPartFormDataContent(formDataContent))
                        }
                        else -> {
                            // No body content
                        }
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
            title = item.title,
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