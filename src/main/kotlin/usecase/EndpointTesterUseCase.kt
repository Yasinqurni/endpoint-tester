package usecase

import model.EndpointCallItem
import model.EndpointCallResult
import model.EndpointTesterRequest
import model.EndpointTesterResponse
import repository.EndpointRepository
import repository.LoggingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface EndpointTesterUseCase {
    suspend fun processEndpointTests(request: EndpointTesterRequest): Result<EndpointTesterResponse>
}

class EndpointTesterUseCaseImpl(
    private val endpointRepository: EndpointRepository,
    private val loggingRepository: LoggingRepository
) : EndpointTesterUseCase {

    override suspend fun processEndpointTests(request: EndpointTesterRequest): Result<EndpointTesterResponse> = 
        runCatching {
            coroutineScope {
                val jobs = request.data.map { item ->
                    async {
                        processEndpointCall(item)
                    }
                }
                
                val results = jobs.map { it.await() }
                
                EndpointTesterResponse(
                    results = results,
                    message = "Processed ${results.size} request(s)"
                )
            }
        }

    private suspend fun processEndpointCall(item: EndpointCallItem): EndpointCallResult {
        // Log the request
        loggingRepository.writeRequest(item)
        
        // Execute the HTTP call
        val result = endpointRepository.execute(item)
        
        // Log the response
        loggingRepository.writeResult(result)
        
        return result
    }
}
