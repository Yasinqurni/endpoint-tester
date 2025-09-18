package controller

import model.StressTestRequest
import model.StressTestResult
import usecase.StressTestUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface StressTestControllerInterface {
    suspend fun executeStressTest(request: StressTestRequest): Result<StressTestResult>
}

class StressTestController : StressTestControllerInterface, KoinComponent {
    private val stressTestUseCase: StressTestUseCase by inject()
    
    override suspend fun executeStressTest(request: StressTestRequest): Result<StressTestResult> {
        return stressTestUseCase.executeStressTest(request)
    }
}
