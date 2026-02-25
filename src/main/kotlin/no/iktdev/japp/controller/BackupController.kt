package no.iktdev.japp.controller

import no.iktdev.japp.service.BackupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/backup")
class BackupController(
    private val backup: BackupService
) {

    @PostMapping("/add")
    suspend fun add(@RequestParam path: String): ResponseEntity<Any> {
        val result = backup.add(path)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/remove")
    suspend fun remove(@RequestParam path: String): ResponseEntity<Any> {
        val result = backup.remove(path)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/status")
    suspend fun status(): ResponseEntity<Any> {
        return ResponseEntity.ok(backup.status())
    }

    @PostMapping("/scan")
    suspend fun scan(): ResponseEntity<Any> {
        return ResponseEntity.ok(backup.scan())
    }

    @PostMapping("/pause")
    suspend fun pause(@RequestParam(required = false) duration: String?): ResponseEntity<Any> {
        return ResponseEntity.ok(backup.pause(duration))
    }

    @PostMapping("/resume")
    suspend fun resume(): ResponseEntity<Any> {
        return ResponseEntity.ok(backup.resume())
    }
}
