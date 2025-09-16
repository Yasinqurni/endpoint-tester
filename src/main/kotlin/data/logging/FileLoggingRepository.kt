package repository

import domain.model.EndpointCallItem
import domain.model.EndpointCallResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.OffsetDateTime

interface LoggingRepository {
    suspend fun writeRequest(item: EndpointCallItem)
    suspend fun writeResult(result: EndpointCallResult)
}

class FileLoggingRepository(
    private val logDir: Path,
    private val json: Json = Json { prettyPrint = true }
) : LoggingRepository {

    init {
        if (!Files.exists(logDir)) {
            Files.createDirectories(logDir)
        }
    }

    override suspend fun writeRequest(item: EndpointCallItem) {
        val line = buildString {
            append(OffsetDateTime.now().toString())
            append(" | REQUEST | ")
            append(json.encodeToString(item))
            append('\n')
        }
        Files.write(logFilePath(), line.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    override suspend fun writeResult(result: EndpointCallResult) {
        val line = buildString {
            append(OffsetDateTime.now().toString())
            append(" | RESPONSE | ")
            append(json.encodeToString(result))
            append('\n')
        }
        Files.write(logFilePath(), line.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    private fun logFilePath(): Path = logDir.resolve("endpoint-tester.log")
}


