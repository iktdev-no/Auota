package no.iktdev.auota.controller

import no.iktdev.auota.models.JottaTransfer
import no.iktdev.auota.service.JottaTransferService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Paths

@RestController
@RequestMapping("/api/transfer")
class TransferController(
    private val transferService: JottaTransferService,
) {

    @PostMapping("/upload", consumes = ["text/plain"])
    suspend fun upload(@RequestBody path: String): ResponseEntity<Any> {
        val result = transferService.upload(Paths.get(path.trim()))

        return when (result) {
            is JottaTransferService.TransferResult.Success -> ResponseEntity.ok(result.output)
            is JottaTransferService.TransferResult.Error -> ResponseEntity.status(500).body(result.output)
        }
    }

    @PostMapping("/download", consumes = ["text/plain"])
    suspend fun  download(@RequestBody path: String): ResponseEntity<Any> {
        val result = transferService.download(path, null)
        return when (result) {
            is JottaTransferService.TransferResult.Success -> ResponseEntity.ok(result.output)
            is JottaTransferService.TransferResult.Error -> ResponseEntity.status(500).body(result.output)
        }
    }

    @GetMapping("/upload/list")
    suspend fun listUploads(): ResponseEntity<List<JottaTransfer>> {
        val result = transferService.listUploads()
        return ResponseEntity.ok(result)
    }

    @GetMapping("/download/list")
    suspend fun listDownloads(): ResponseEntity<List<JottaTransfer>> {
        val result = transferService.listDownloads()
        return ResponseEntity.ok(result)
    }
}