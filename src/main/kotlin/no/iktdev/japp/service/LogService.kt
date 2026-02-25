package no.iktdev.japp.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.models.LogfileResponse
import org.springframework.stereotype.Service
import java.io.File
import java.io.RandomAccessFile
import kotlinx.coroutines.delay

@Service
class LogService(
    private val cli: JottaCli
) {

    fun streamFile(path: String, pollMs: Long = 500L): Flow<String> = flow {
        val file = File(path)
        var pointer = 0L

        // --- INIT: les hele eksisterende fil ---
        if (file.exists() && file.isFile) {
            RandomAccessFile(file, "r").use { raf ->
                var line = raf.readLine()
                while (line != null) {
                    emit(line.toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8))
                    line = raf.readLine()
                }
                pointer = raf.filePointer
            }
        }

        // --- CONTINUOUS TAIL ---
        while (true) {
            if (!file.exists()) {
                delay(pollMs)
                continue
            }

            val len = file.length()
            if (len < pointer) pointer = 0L // logrotate

            if (len > pointer) {
                RandomAccessFile(file, "r").use { raf ->
                    raf.seek(pointer)
                    var line = raf.readLine()
                    while (line != null) {
                        emit(line.toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8))
                        line = raf.readLine()
                    }
                    pointer = raf.filePointer
                }
            }

            delay(pollMs)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun streamJottaLog(): Flow<String> {
        val result = cli.run("logfile")

        if (result !is JottaCli.RunResult.Success) {
            return flow { emit("Failed to execute jotta-cli logfile") }
        }

        val lines = result.output
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // Finn første linje som ser ut som en path
        val path = lines.firstOrNull { line ->
            line.startsWith("/") && File(line).exists()
        }

        return if (path != null) {
            streamFile(path)
        } else {
            flow {
                emit("Could not determine Jotta logfile path.")
                emit("Raw output:")
                lines.forEach { emit(it) }
            }
        }
    }

    fun listAvailableLogs(): List<String> {
        // Tilpass pathene til dine systemlogger
        return listOf("/var/log/auota/spring.log")
    }
}