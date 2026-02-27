package no.iktdev.auota.controller

import no.iktdev.auota.models.jottaFs.JottaFs
import no.iktdev.auota.service.JottaFileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/jfiles")
class JottaFileExplorerController(
    private val jottafs: JottaFileService
) {
    @GetMapping()
    suspend fun roots(): ResponseEntity<JottaFs> {
        val result = jottafs.explore("")
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @GetMapping("/explore")
    suspend fun list(@RequestParam path: String): ResponseEntity<JottaFs> {
        val result = jottafs.explore(path)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}