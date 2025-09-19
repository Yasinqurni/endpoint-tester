package com.example

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import org.koin.ktor.plugin.Koin
import org.koin.dsl.module
import repository.EndpointRepository
import repository.EndpointRepositoryImpl
import repository.LoggingRepositoryImpl
import repository.LoggingRepository
import usecase.EndpointTesterUseCase
import usecase.EndpointTesterUseCaseImpl
import usecase.StressTestUseCase
import usecase.StressTestUseCaseImpl
import controller.EndpointTesterController
import controller.StressTestController
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import kotlinx.serialization.json.Json
import router.registerEndpointTesterRoute
import router.registerStressTestRoute
import java.nio.file.Path
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; prettyPrint = true })
    }
    install(Koin) {
        modules(appModule)
    }
    configureRouting()
    registerEndpointTesterRoute()
    registerStressTestRoute()
}

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respondText("Hello World!")
        }
    }
}

private val appModule = module {
    // Repositories
    single {
        HttpClient(CIO) {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single<EndpointRepository> { EndpointRepositoryImpl(get()) }
    single<LoggingRepository> { LoggingRepositoryImpl(Path.of("log")) }
    
    // Use Cases
    single<EndpointTesterUseCase> { EndpointTesterUseCaseImpl(get(), get()) }
    single<StressTestUseCase> { StressTestUseCaseImpl(get(), get()) }
    
    // Controllers
    single { EndpointTesterController() }
    single { StressTestController() }
}
