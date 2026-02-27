package no.iktdev.auota.service

import com.google.gson.Gson
import com.squareup.moshi.Moshi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import no.iktdev.auota.cli.JottaCli
import no.iktdev.auota.cli.JottaCliClient
import no.iktdev.auota.models.jottaFs.JottaFs
import org.springframework.stereotype.Service
import java.io.File

@Service
class JottaFileService(
    private val jottaCli: JottaCliClient,
    private val moshi: Moshi,
) {
    private val gson = Gson()
    private val log = KotlinLogging.logger {}

    suspend fun explore(path: String): JottaFs? {
        val result = if (path.isBlank()) {
            jottaCli.run("ls", "--json")
        } else {
            jottaCli.run("ls", path, "--json")
        }
        if (result.exitCode != 0) {
            log.error { "Failed to list items at $path (exit code: ${result.exitCode})" }
            return null
        }

        val jfs = try {
            moshi.adapter(JottaFs::class.java).fromJson(result.output)
        } catch (e: Exception) {
            if (isEmptyFolder(result.output)) {
                return JottaFs(emptyList(), emptyList())
            }
            log.error("Failed to read Jotta File data on $path", e)
            null
        }
        return jfs?.applyAttributes()?.appyAssumedPathForFolders()
    }
    fun JottaFs.applyAttributes(): JottaFs {
        this.Files?.onEach { file ->
            file.extension = File(file.Path).extension
        }
        return this
    }

    fun JottaFs.appyAssumedPathForFolders(): JottaFs {
        val adjusted = this.Folders?.map { folder ->
            if (folder.Path.isNullOrBlank() && folder.Name.lowercase() == "backup") {
                folder.copy(Path = "/backup/")
            } else folder
        }
        return this.copy(Folders = adjusted)
    }

    private fun isEmptyFolder(output: String): Boolean {
        val patterns = listOf(
            "nothing found",
            "maybe its a file"
        )

        val lowerOutput = output.lowercase()
        return patterns.any { lowerOutput.contains(it) }
    }
}