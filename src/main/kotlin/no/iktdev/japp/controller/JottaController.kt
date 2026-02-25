package no.iktdev.japp.controller

import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.jotta.JottaVersion
import no.iktdev.japp.models.JottaVersionInfo
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
