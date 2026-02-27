package no.iktdev.auota.crypt.encrypt.operations

import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.models.GocryptfsConfigExport
import no.iktdev.auota.sha256Hex
import java.nio.file.Files
import java.util.Base64

class ConfigExportOperation(
    private val paths: BackendPaths
) {
    fun export(): GocryptfsConfigExport? {
        if (!Files.exists(paths.config)) return null

        val bytes = Files.readAllBytes(paths.config)
        val base64 = Base64.getEncoder().encodeToString(bytes)
        val sha256 = sha256Hex(bytes)

        return GocryptfsConfigExport(
            base64Config = base64,
            sha256 = sha256
        )
    }
}
