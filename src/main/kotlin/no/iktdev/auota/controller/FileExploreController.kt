package no.iktdev.auota.controller

import mu.KotlinLogging
import no.iktdev.auota.models.files.Roots
import no.iktdev.auota.models.files.IFile
import no.iktdev.auota.models.files.JottaFs
import no.iktdev.auota.service.ExplorerService
import no.iktdev.auota.service.JottaFileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder

@RestController
@RequestMapping("/api/files")
class FileExploreController(
    private val explorer: ExplorerService,
    private val jottafs: JottaFileService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/roots")
    suspend fun roots(): ResponseEntity<List<Roots>> =
        ResponseEntity.ok(explorer.listRoots())

    @GetMapping("/list")
    fun listLocal(@RequestParam path: String): ResponseEntity<List<IFile>> {
        val decoded = URLDecoder.decode(path, Charsets.UTF_8)

        return ResponseEntity.ok(explorer.listAt(decoded))
    }


    @GetMapping("/jotta")
    suspend fun listJotta(@RequestParam path: String): ResponseEntity<JottaFs> {
        val decoded = URLDecoder.decode(path, Charsets.UTF_8)

        val result = jottafs.explore(decoded)
        return if (result != null) ResponseEntity.ok(result)
        else ResponseEntity.notFound().build()
    }


}