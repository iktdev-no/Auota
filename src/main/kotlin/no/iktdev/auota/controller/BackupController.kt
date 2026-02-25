package no.iktdev.auota.controller

import no.iktdev.auota.models.backup.BackupIEUpdate
import no.iktdev.auota.models.backup.BackupItem
import no.iktdev.auota.service.BackupService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/backup")
class BackupController(
    private val backup: BackupService
) {

    @PostMapping(
        "/add", consumes = [MediaType.TEXT_PLAIN_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun add(@RequestBody path: String): ResponseEntity<Any> {
        val result = backup.add(path)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/remove", consumes = [MediaType.TEXT_PLAIN_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun remove(@RequestBody path: String): ResponseEntity<Any> {
        val result = backup.remove(path)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/exclude")
    suspend fun exclude(@RequestBody payload: BackupIEUpdate): ResponseEntity<Any> {
        val result = backup.ignore(payload)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/include")
    suspend fun include(@RequestBody payload: BackupIEUpdate): ResponseEntity<Any> {
        val result = backup.unignore(payload)
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
    suspend fun pause(@RequestBody(required = false) duration: String?): ResponseEntity<Any> {
        return ResponseEntity.ok(backup.pause(duration))
    }

    @PostMapping("/resume")
    suspend fun resume(): ResponseEntity<Any> {
        return ResponseEntity.ok(backup.resume())
    }

    @GetMapping("/list")
    fun list(): ResponseEntity<List<BackupItem>> {
        return ResponseEntity.ok(backup.getFolders())
    }
}
