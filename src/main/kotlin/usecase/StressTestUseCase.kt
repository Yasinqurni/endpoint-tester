package usecase

import model.*
import repository.EndpointRepository
import repository.LoggingRepository
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

interface StressTestUseCase {
    suspend fun executeStressTest(request: StressTestRequest): Result<StressTestResult>
}

class StressTestUseCaseImpl(
    private val endpointRepository: EndpointRepository,
    private val loggingRepository: LoggingRepository
) : StressTestUseCase {

    override suspend fun executeStressTest(request: StressTestRequest): Result<StressTestResult> = 
        runCatching {
            val config = request.config
            val headers = request.headers
            
            // Create endpoint call item from config
            val endpointItem = EndpointCallItem(
                endpointUrl = config.endpointUrl,
                httpMethod = config.httpMethod,
                requestBody = config.requestBody,
                formData = config.formData,
                fileData = config.fileData
            )
            
            // Execute stress test
            val startTime = System.currentTimeMillis()
            val results = mutableListOf<EndpointCallResult>()
            val responseTimes = mutableListOf<Long>()
            
            // Calculate ramp-up strategy
            val rampUpDelay = if (config.rampUpTimeSeconds > 0) {
                (config.rampUpTimeSeconds * 1000L) / config.concurrentUsers
            } else 0L
            
            // Execute requests in batches to control concurrency
            val batchSize = config.concurrentUsers
            val totalBatches = (config.totalRequests + batchSize - 1) / batchSize
            
            for (batch in 0 until totalBatches) {
                val batchStart = batch * batchSize
                val batchEnd = minOf(batchStart + batchSize, config.totalRequests)
                
                // Ramp-up delay for first few batches
                if (batch < config.concurrentUsers && rampUpDelay > 0) {
                    delay(rampUpDelay)
                }
                
                // Execute batch concurrently
                coroutineScope {
                    val batchJobs = (batchStart until batchEnd).map { i ->
                        async {
                            val result = executeSingleRequest(endpointItem, headers)
                            synchronized(results) {
                                results.add(result)
                                responseTimes.add(result.durationMs)
                            }
                        }
                    }
                    
                    // Wait for all jobs in this batch to complete
                    batchJobs.awaitAll()
                }
            }
            
            val endTime = System.currentTimeMillis()
            val totalDuration = endTime - startTime
            
            // Calculate metrics
            val metrics = calculateMetrics(results, responseTimes, totalDuration)
            
            // Log stress test results
            loggingRepository.writeStressTestResult(config, metrics, results)
            
            StressTestResult(
                config = config,
                metrics = metrics,
                results = results,
                message = "Stress test completed: ${metrics.successfulRequests}/${metrics.totalRequests} successful"
            )
        }

    private suspend fun executeSingleRequest(
        item: EndpointCallItem, 
        headers: Headers
    ): EndpointCallResult {
        return try {
            endpointRepository.execute(item, headers)
        } catch (e: Exception) {
            EndpointCallResult(
                endpoint = item.endpointUrl,
                method = item.httpMethod,
                statusCode = -1,
                success = false,
                durationMs = 0,
                responseHeaders = emptyMap(),
                responseBody = e.message
            )
        }
    }

    private fun calculateMetrics(
        results: List<EndpointCallResult>,
        responseTimes: List<Long>,
        totalDuration: Long
    ): StressTestMetrics {
        val totalRequests = results.size
        val successfulRequests = results.count { it.success }
        val failedRequests = totalRequests - successfulRequests
        
        val avgResponseTime = if (responseTimes.isNotEmpty()) {
            responseTimes.average()
        } else 0.0
        
        val minResponseTime = responseTimes.minOrNull() ?: 0L
        val maxResponseTime = responseTimes.maxOrNull() ?: 0L
        
        val requestsPerSecond = if (totalDuration > 0) {
            (totalRequests * 1000.0) / totalDuration
        } else 0.0
        
        val errorRate = if (totalRequests > 0) {
            (failedRequests.toDouble() / totalRequests) * 100.0
        } else 0.0
        
        return StressTestMetrics(
            totalRequests = totalRequests,
            successfulRequests = successfulRequests,
            failedRequests = failedRequests,
            averageResponseTime = avgResponseTime,
            minResponseTime = minResponseTime,
            maxResponseTime = maxResponseTime,
            requestsPerSecond = requestsPerSecond,
            errorRate = errorRate,
            duration = totalDuration
        )
    }
}