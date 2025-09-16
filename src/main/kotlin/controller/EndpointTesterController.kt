package controller

import model.EndpointTesterRequest
import model.EndpointTesterResponse
import usecase.EndpointTesterUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface EndpointTesterControllerInterface {
    suspend fun processEndpointTests(request: EndpointTesterRequest): Result<EndpointTesterResponse>
}

class EndpointTesterController : EndpointTesterControllerInterface, KoinComponent {
    private val endpointTesterUseCase: EndpointTesterUseCase by inject()
    
    override suspend fun processEndpointTests(request: EndpointTesterRequest): Result<EndpointTesterResponse> {
        return endpointTesterUseCase.processEndpointTests(request)
    }
}
