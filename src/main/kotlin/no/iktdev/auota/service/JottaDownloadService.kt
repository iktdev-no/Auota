package no.iktdev.auota.service

import mu.KotlinLogging
import no.iktdev.auota.cli.JottaCli
import no.iktdev.auota.cli.JottaCliClient
import org.springframework.stereotype.Service

@Service
class JottaDownloadService(
    private val cli: JottaCliClient,
) {
    private val log = KotlinLogging.logger {}

    suspend fun download(path: String): ByteArray? {
        log.info { "Downloading: $path" }

        val raw = when (val result = cli.run("download", "-O", path)) {
            is JottaCli.RunResult.Success ->
                result.output.toByteArray(Charsets.ISO_8859_1)

            is JottaCli.RunResult.Error ->
                throw IllegalStateException("Download failed: ${result.output}")
        }

        return if (normalizeBackupPath(path)) {
            log.info { "Decrypting backup file: $path" }
            null
        } else {
            raw
        }
    }

    fun normalizeBackupPath(path: String): Boolean {
        // Fjern leading slash
        val p = path.removePrefix("/")

        // Split root fra resten
        val firstSlash = p.indexOf('/')
        val root = if (firstSlash == -1) p else p.substring(0, firstSlash)
        val rest = if (firstSlash == -1) "" else p.substring(firstSlash + 1)

        // Kun backup skal normaliseres
        return root.equals("backup", ignoreCase = true)
    }

}
