package no.iktdev.japp.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.models.LogfileResponse
import org.springframework.stereotype.Service
import java.io.File

@Service
class LogService(
    private val cli: JottaCli
) {

    suspend fun getLogfile(): LogfileResponse {
        return when (val result = cli.run("logfile")) {
            is JottaCli.RunResult.Success -> {
                val path = result.output.trim()
                LogfileResponse(true, path)
            }
            is JottaCli.RunResult.Error ->
                LogfileResponse(false, null, "Failed to get logfile: ${result.output}")
        }
    }

    suspend fun readLogfile(): String? {
        val logfile = getLogfile()
        if (!logfile.success || logfile.path == null) return null

        val file = File(logfile.path)
        if (!file.exists()) return null

        return withContext(Dispatchers.IO) {
            file.readText()
        }
    }

    suspend fun streamLogfile(onLine: (String) -> Unit): Int {
        val logfile = getLogfile()

        if (!logfile.success || logfile.path == null) {
            onLine("Could not determine logfile path.")
            return -1
        }

        val file = File(logfile.path)
        if (!file.exists()) {
            onLine("Logfile does not exist: ${logfile.path}")
            return -1
        }

        return cli.stream("tail", "-f", logfile.path) { line ->
            onLine(line)
        }.exitCode
    }
}
