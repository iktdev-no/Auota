package no.iktdev.auota.controller

import no.iktdev.auota.jotta.JottaVersion
import no.iktdev.auota.models.JottaVersionInfo
import no.iktdev.auota.models.JottadStatus
import no.iktdev.auota.service.JottadMonitor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/jotta")
class JottaController(
    private val jottaVersion: JottaVersion
) {

    @GetMapping("/version")
    suspend fun version(): JottaVersionInfo {
        return jottaVersion.getVersion()
    }
}
