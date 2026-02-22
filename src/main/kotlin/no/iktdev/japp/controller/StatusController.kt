package no.iktdev.japp.controller

import no.iktdev.japp.models.StatusResponse
import no.iktdev.japp.service.StatusService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/status")
class StatusController(
    private val statusService: StatusService
) {
    @GetMapping
    suspend fun getStatus(): ResponseEntity<StatusResponse> {
        return ResponseEntity.ok(statusService.getStatus())
    }
}

