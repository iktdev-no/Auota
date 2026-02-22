package no.iktdev.japp.controller

import no.iktdev.japp.models.EncryptionConfig
import no.iktdev.japp.models.EncryptionStatus
import no.iktdev.japp.service.EncryptionManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/encryption")
class EncryptionController(
    private val manager: EncryptionManager
) {

    @GetMapping("/status")
    fun status(): ResponseEntity<EncryptionStatus> =
        ResponseEntity.ok(manager.getStatus())

    @PostMapping("/config")
    fun updateConfig(@RequestBody cfg: EncryptionConfig): ResponseEntity<EncryptionStatus> {
        manager.saveConfig(cfg)
        return ResponseEntity.ok(manager.getStatus())
    }

    @PostMapping("/init")
    fun init(): ResponseEntity<EncryptionStatus> {
        val cfg = manager.loadConfig()
        manager.init(cfg)
        return ResponseEntity.ok(manager.getStatus())
    }

    @PostMapping("/mount")
    fun mount(): ResponseEntity<EncryptionStatus> {
        val cfg = manager.loadConfig()
        manager.mount(cfg)
        return ResponseEntity.ok(manager.getStatus())
    }

    @PostMapping("/unmount")
    fun unmount(): ResponseEntity<EncryptionStatus> {
        manager.unmount()
        return ResponseEntity.ok(manager.getStatus())
    }
}
