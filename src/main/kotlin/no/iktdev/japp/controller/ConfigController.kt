package no.iktdev.japp.controller

import no.iktdev.japp.models.JottaConfig
import no.iktdev.japp.service.ConfigService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/config")
class ConfigController(
    private val configService: ConfigService
) {

    @GetMapping
    suspend fun getConfig(): ResponseEntity<JottaConfig> =
        ResponseEntity.ok(configService.getConfig())

    @PostMapping("/set")
    suspend fun setConfig(@RequestParam key: String, @RequestParam value: String): ResponseEntity<Boolean> =
        ResponseEntity.ok(configService.set(key, value))
}
