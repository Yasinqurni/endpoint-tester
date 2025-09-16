package controller

import domain.model.EndpointTesterRequest
import domain.model.EndpointTesterResponse
import service.EndpointTesterService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EndpointTesterController : KoinComponent {
    private val endpointTesterService: EndpointTesterService by inject()
    
    suspend fun processEndpointTests(request: EndpointTesterRequest): EndpointTesterResponse {
        return endpointTesterService.processEndpointTests(request)
    }
}
