package no.iktdev.auota.controller

import no.iktdev.auota.crypt.encrypt.EncryptionManager
import no.iktdev.auota.models.EncryptionStatus
import no.iktdev.auota.models.JottaSummary
import no.iktdev.auota.models.JottadStatus
import no.iktdev.auota.service.JottadMonitor
import no.iktdev.auota.service.status.JottaStatusService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/status")
class StatusController(
    private val jottaStatusService: JottaStatusService,
    private val encryptionManager: EncryptionManager,
    private val jottadMonitor: JottadMonitor
) {
    @GetMapping("/jotta")
    suspend fun getStatus(): ResponseEntity<JottaSummary> {
        return ResponseEntity.ok(jottaStatusService.getStatus())
    }

    @GetMapping("/encryption")
    suspend fun getEncryptionStatus(): ResponseEntity<EncryptionStatus> {
        return ResponseEntity.ok(encryptionManager.getStatus())
    }

    @GetMapping("/daemon")
    fun getDaemonStatus(): JottadStatus {
        return jottadMonitor.getJottaDaemonStatus()
    }
}

