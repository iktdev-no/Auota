package no.iktdev.auota.encrypt.operations

import no.iktdev.auota.encrypt.backend.BackendPaths
import no.iktdev.auota.models.GocryptfsConfigExport
import no.iktdev.auota.sha256Hex
import java.nio.file.Files
import java.util.Base64

class ConfigImportOperation(
    private val paths: BackendPaths
) {
    fun importConfig(export: GocryptfsConfigExport): Boolean {
        val bytes = Base64.getDecoder().decode(export.base64Config)
        val actualSha = sha256Hex(bytes)

        if (!actualSha.equals(export.sha256, ignoreCase = true)) {
            return false
        }

        Files.createDirectories(paths.backend)
        Files.write(paths.config, bytes)

        return true
    }
}
