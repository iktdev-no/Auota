package no.iktdev.japp.controller

import no.iktdev.japp.encrypt.EncryptionManager
import no.iktdev.japp.models.EncryptionStatus
import no.iktdev.japp.models.JottaSummary
import no.iktdev.japp.service.status.JottaStatusService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/status")
class StatusController(
    private val jottaStatusService: JottaStatusService,
    private val encryptionManager: EncryptionManager
) {
    @GetMapping("/jotta")
    suspend fun getStatus(): ResponseEntity<JottaSummary> {
        return ResponseEntity.ok(jottaStatusService.getStatus())
    }

    @GetMapping("/encryption")
    suspend fun getEncryptionStatus(): ResponseEntity<EncryptionStatus> {
        return ResponseEntity.ok(encryptionManager.getStatus())
    }
}

