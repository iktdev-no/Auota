package no.iktdev.japp.controller

import no.iktdev.japp.service.BackupService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/backup")
class BackupController(
    private val backup: BackupService
) {

    @PostMapping("/add")
    suspend fun add() = backup.add()

    @GetMapping("/status")
    suspend fun status() = backup.status()

    @PostMapping("/scan")
    suspend fun scan() = backup.scan()

    @PostMapping("/pause")
    suspend fun pause(@RequestParam(required = false) duration: String?) =
        backup.pause(duration)

    @PostMapping("/resume")
    suspend fun resume() = backup.resume()

    @DeleteMapping
    suspend fun remove() = backup.remove()
}

