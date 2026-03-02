package no.iktdev.auota.controller.crypt

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.crypt.encrypt.EncryptionManager
import no.iktdev.auota.models.EncryptionStatus
import no.iktdev.auota.models.GocryptfsConfigExport
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/encryption")
class EncryptionController(
    private val manager: EncryptionManager
): CryptController(manager) {
    override val log = KotlinLogging.logger {}


    @PostMapping("/encrypt")
    suspend fun encrypt(@RequestBody enabled: Boolean): ResponseEntity<EncryptionStatus> {
        val current = manager.loadConfig()
        val updated = current.copy(enabled = enabled)
        log.info { "Changing Encrypting from ${current.enabled} to ${updated.enabled}" }
        manager.applyConfig(updated)
        return ResponseEntity.ok(manager.getStatus())
    }

}
