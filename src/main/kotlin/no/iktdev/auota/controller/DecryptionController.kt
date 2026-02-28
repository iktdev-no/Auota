package no.iktdev.auota.controller

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.iktdev.auota.crypt.decrypt.DecryptionManager
import no.iktdev.auota.crypt.encrypt.EncryptionManager
import no.iktdev.auota.models.EncryptionStatus
import no.iktdev.auota.models.GocryptfsConfigExport
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/decryption")
class DecryptionController(
    private val manager: DecryptionManager
) {
    private val log = KotlinLogging.logger {}


    @GetMapping("/status")
    fun status(): ResponseEntity<EncryptionStatus> =
        ResponseEntity.ok(manager.getStatus())

    @PostMapping("/encrypt")
    suspend fun encrypt(@RequestBody enabled: Boolean): ResponseEntity<EncryptionStatus> {
        val current = manager.loadConfig()
        val updated = current.copy(enabled = enabled)
        log.info { "Changing Encrypting from ${current.enabled} to ${updated.enabled}" }
        manager.applyConfig(updated)
        return ResponseEntity.ok(manager.getStatus())
    }

    @PostMapping("/password", consumes = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun updatePassword(@RequestBody password: String): ResponseEntity<EncryptionStatus> {
        val updated = manager.loadConfig().copy(password = password)
        manager.applyConfig(updated)
        return ResponseEntity.ok(manager.getStatus())
    }

    @PostMapping("/override/enable")
    fun enableOverride(): ResponseEntity<EncryptionStatus> {
        val status = manager.enableManualOverride()
        return if (status)
             ResponseEntity.ok(manager.getStatus())
        else ResponseEntity.status(HttpStatus.LOCKED).body(manager.getStatus())
    }

    @PostMapping("/override/disable")
    fun disableOverride(): ResponseEntity<EncryptionStatus> {
        manager.disableManualOverride()
        return ResponseEntity.ok(manager.getStatus())
    }

    @PostMapping("/mount/manual")
    suspend fun manualMount(): ResponseEntity<EncryptionStatus> {
        manager.manualMount()
        return ResponseEntity.ok(manager.getStatus())
    }

    @PostMapping("/unmount/manual")
    suspend fun manualUnmount(): ResponseEntity<EncryptionStatus> {
        manager.manualUnmount()
        return ResponseEntity.ok(manager.getStatus())
    }


    private val gson = Gson()

    // ------------------------------------------------------------
    // EXPORT STORE (gocryptfs.conf)
    // ------------------------------------------------------------
    @GetMapping("/export/store")
    suspend fun export(): ResponseEntity<ByteArray> {
        val export = manager.exportGocryptfsConfig()
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("gocryptfs.conf not found".toByteArray())

        val json = gson.toJson(export)
        val filename = "AuotaStore.key"

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"$filename\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(json.toByteArray(Charsets.UTF_8))
    }

    // ------------------------------------------------------------
    // IMPORT STORE (gocryptfs.conf)
    // ------------------------------------------------------------
    @PostMapping("/import/store")
    suspend fun import(@RequestBody body: String): ResponseEntity<EncryptionStatus> {
        try {
            val data = gson.fromJson(body, GocryptfsConfigExport::class.java)

            val ok = manager.importGocryptfsConfig(data)
            if (!ok) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(manager.getStatus().copy(reason = "Invalid or corrupted config"))
            }

            // Re-init systemet etter import
            GlobalScope.launch {
                manager.autoInitAsync()
            }

            return ResponseEntity.ok(manager.getStatus())

        } catch (e: JsonSyntaxException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(manager.getStatus().copy(reason = "Malformed import file"))
        }
    }

    @DeleteMapping("/store")
    suspend fun deleteStore(): ResponseEntity<EncryptionStatus> {
        val ok = manager.resetGocryptfsConfig()

        if (!ok) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(manager.getStatus().copy(reason = "Failed to reset gocryptfs config"))
        }

        // Etter reset må systemet reinitialiseres
        GlobalScope.launch {
            manager.autoInitAsync()
        }

        return ResponseEntity.ok(manager.getStatus())
    }



}
