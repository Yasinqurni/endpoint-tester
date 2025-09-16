package com.example

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import org.koin.ktor.plugin.Koin
import org.koin.dsl.module
import repository.EndpointRepository
import repository.KtorEndpointRepository
import repository.FileLoggingRepository
import repository.LoggingRepository
import service.EndpointTesterService
import controller.EndpointTesterController
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import kotlinx.serialization.json.Json
import router.registerEndpointTesterRoute
import java.nio.file.Path

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
    single<EndpointRepository> { KtorEndpointRepository(get()) }
    single<LoggingRepository> { FileLoggingRepository(Path.of("build/logs")) }
    
    // Services
    single { EndpointTesterService(get(), get()) }
    
    // Controllers
    single { EndpointTesterController() }
}
