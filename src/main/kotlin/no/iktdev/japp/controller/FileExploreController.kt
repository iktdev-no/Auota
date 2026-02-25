package no.iktdev.japp.controller

import mu.KotlinLogging
import no.iktdev.japp.models.file.IFile
import no.iktdev.japp.service.ExplorerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File

@RestController
@RequestMapping("/api/files")
class FileExploreController(
    private val explorer: ExplorerService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/roots")
    fun roots(): ResponseEntity<List<IFile>> {
        return ResponseEntity.ok(
            explorer.listAt("/")
        )
    }

    @GetMapping("/explore")
    fun list(@RequestParam path: String): ResponseEntity<List<IFile>> {
        val file = File(path)
        if (!file.exists() || file.isFile) {
            return ResponseEntity.notFound().build()
        }
        val files = explorer.listAt(path)
        return ResponseEntity.ok(files)
    }

}