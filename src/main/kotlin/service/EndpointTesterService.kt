package service

import domain.model.EndpointCallItem
import domain.model.EndpointCallResult
import domain.model.EndpointTesterRequest
import domain.model.EndpointTesterResponse
import repository.EndpointRepository
import repository.LoggingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class EndpointTesterService(
    private val endpointRepository: EndpointRepository,
    private val loggingRepository: LoggingRepository
) {
    suspend fun processEndpointTests(request: EndpointTesterRequest): EndpointTesterResponse = coroutineScope {
        val jobs = request.data.map { item ->
            async {
                // Log the request
                loggingRepository.writeRequest(item)
                
                // Execute the HTTP call
                val result = endpointRepository.execute(item)
                
                // Log the response
                loggingRepository.writeResult(result)
                
                result
            }
        }
        
        val results = jobs.map { it.await() }
        
        EndpointTesterResponse(
            results = results,
            message = "Processed ${results.size} request(s)"
        )
    }
}
