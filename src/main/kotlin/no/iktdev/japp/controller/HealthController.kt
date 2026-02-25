package no.iktdev.japp.controller

import no.iktdev.japp.models.SystemHealth
import no.iktdev.japp.service.SystemHealthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/health")
class HealthController(
    private val health: SystemHealthService
) {
    @GetMapping
    fun get(): ResponseEntity<SystemHealth> =
        ResponseEntity.ok(health.health.value)
}
