package router

import controller.EndpointTesterController
import domain.model.EndpointTesterRequest
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.registerEndpointTesterRoute() {
    val controller by inject<EndpointTesterController>()

    routing {
        post("/endpoint-tester") {
            val payload = call.receive<EndpointTesterRequest>()
            val result = controller.processEndpointTests(payload)
            call.respond(result)
        }
    }
}


