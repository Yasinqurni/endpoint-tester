package router

import controller.EndpointTesterController
import model.EndpointTesterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

fun Application.registerEndpointTesterRoute() {
    val controller by inject<EndpointTesterController>()

    routing {
        post("/endpoint-tester") {
            val payload = call.receive<EndpointTesterRequest>()
            val result = controller.processEndpointTests(payload)
            
            result.fold(
                onSuccess = { response ->
                    call.respond(HttpStatusCode.OK, response)
                },
                onFailure = { exception ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(
                            error = "Failed to process endpoint tests",
                            message = exception.message ?: "Unknown error"
                        )
                    )
                }
            )
        }
    }
}


