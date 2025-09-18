package router

import controller.StressTestController
import model.StressTestRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import kotlinx.serialization.Serializable

fun Application.registerStressTestRoute() {
    val controller by inject<StressTestController>()

    routing {
        post("/stress-test") {
            val payload = call.receive<StressTestRequest>()
            val result = controller.executeStressTest(payload)
            
            result.fold(
                onSuccess = { response ->
                    call.respond(HttpStatusCode.OK, response)
                },
                onFailure = { exception ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf(
                            "error" to "Failed to execute stress test",
                            "message" to (exception.message ?: "Unknown error")
                        )
                    )
                }
            )
        }
    }
}
