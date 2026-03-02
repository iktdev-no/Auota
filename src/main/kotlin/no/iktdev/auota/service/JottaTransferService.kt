package no.iktdev.auota.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.auota.cli.JottaCli
import no.iktdev.auota.models.JottaTransfer
import no.iktdev.auota.service.status.JottaStatusService
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory

@Service
class JottaTransferService(
    private val cli: JottaCli,
    private val status: JottaStatusService
) {
    private val log = KotlinLogging.logger {}

    private val mapper = jacksonObjectMapper()

    private var _deviceName: String? = null
    fun getDeviceName(): String? {
        return _deviceName
    }


    /**
     * Laster opp en fil eller mappe til Jottacloud Archive.
     *
     * @param localPath Absolutt path til fil eller mappe på disk.
     * @param remotePath Valgfri remote path under Archive/.
     *                   Hvis null → Jottacloud lager destinasjon automatisk under Archive/<device>.
     */
    suspend fun upload(localPath: Path, remotePath: String? = null): TransferResult {
        val args = mutableListOf<String>()

        // Base command
        args += "archive"
        args += localPath.toAbsolutePath().toString()

        // Optional remote destination
        if (!remotePath.isNullOrBlank()) {
            args += "--remote=$remotePath"
        }

        // Always disable GUI in backend
        args += "--nogui"

        // Run CLI
        return when (val result = cli.run(*args.toTypedArray())) {
            is JottaCli.RunResult.Success -> TransferResult.Success(result.output)
            is JottaCli.RunResult.Error -> TransferResult.Error(result.output, result.exitCode)
        }
    }

    /**
     * Streamet upload (nyttig for mapper som tar tid).
     * onLine kalles for hver linje CLI skriver ut.
     */
    suspend fun uploadStreaming(
        localPath: Path,
        remotePath: String? = null,
        onLine: (String) -> Unit
    ): TransferResult {
        val args = mutableListOf<String>()

        args += "archive"
        args += localPath.toAbsolutePath().toString()

        if (!remotePath.isNullOrBlank()) {
            args += "--remote=$remotePath"
        }

        args += "--nogui"

        return when (val result = cli.stream(*args.toTypedArray(), onLine = onLine)) {
            is JottaCli.StreamResult.Success -> TransferResult.Success("OK")
            is JottaCli.StreamResult.Error -> TransferResult.Error("Upload failed", result.exitCode)
        }
    }


    private val encryptedDownloadRoot = Paths.get("/download-encrypted")
    private val normalDownloadRoot = Paths.get("/download")

    /**
     * Laster ned en fil eller mappe fra Jottacloud.
     *
     * @param remotePath Path i Jottacloud (f.eks. "/archive/Kaze - Stable/Test.txt")
     * @param originLocalPath Local path filen opprinnelig ble lastet opp fra (f.eks. "/data/Test.txt")
     */
    suspend fun download(remotePath: String, originLocalPath: String?): TransferResult {
        val targetDir = resolveDownloadTarget(remotePath)

        // Hvis targetDir peker på en fil → bruk parent
        val finalTargetDir = if (targetDir.toString().contains(".")) {
            targetDir.parent
        } else {
            targetDir
        }

        // Sørg for at mappen finnes
        withContext(Dispatchers.IO) {
            Files.createDirectories(finalTargetDir)
        }

        log.info { "⏬ Starter nedlasting" }
        log.info { "  Remote path: $remotePath" }
        log.info { "  Target dir:  $finalTargetDir" }

        val args = listOf(
            "download",
            remotePath,
            finalTargetDir.toAbsolutePath().toString()
        )

        log.info { "  Kjører CLI: jotta-cli ${args.joinToString(" ")}" }

        return when (val result = cli.run(*args.toTypedArray())) {
            is JottaCli.RunResult.Success -> {
                log.info { "✅ Download success:\n${result.output}" }
                TransferResult.Success(result.output)
            }
            is JottaCli.RunResult.Error -> {
                log.error { "❌ Download failed (exit=${result.exitCode}):\n${result.output}" }
                TransferResult.Error(result.output, result.exitCode)
            }
        }
    }




    suspend fun listUploads(): List<JottaTransfer> {
        return when (val result = cli.run("list", "uploads", "--json")) {
            is JottaCli.RunResult.Success -> {
                mapper.readValue(result.output)
            }
            is JottaCli.RunResult.Error -> {
                throw IllegalStateException("Failed to list uploads: ${result.output}")
            }
        }
    }

    suspend fun listDownloads(): List<JottaTransfer> {
        return when (val result = cli.run("list", "downloads", "--json")) {
            is JottaCli.RunResult.Success -> {
                mapper.readValue(result.output)
            }
            is JottaCli.RunResult.Error -> {
                throw IllegalStateException("Failed to list downloads: ${result.output}")
            }
        }
    }

    /**
     * Bestemmer hvor filen skal lastes ned basert på origin-path.
     */
    private fun resolveDownloadTarget(remotePath: String): Path {
        val deviceName = status.getDeviceName() ?: ""
        val remoteLower = remotePath.lowercase()
        val prefixLower = "/backup/${deviceName.lowercase()}/"

        return if (remoteLower.startsWith(prefixLower)) {
            // Strip "/backup/<deviceName>/" og bruk resten
            val relative = remotePath.substring(prefixLower.length)
            encryptedDownloadRoot.resolve(relative)
        } else {
            // Ingen stripping – bare la pathen gå rett igjennom
            normalDownloadRoot.resolve(remotePath.removePrefix("/"))
        }
    }




    sealed class TransferResult {
        data class Success(val output: String) : TransferResult()
        data class Error(val output: String, val exitCode: Int) : TransferResult()
    }
}