package no.iktdev.japp.controller

import no.iktdev.japp.models.OperationRequest
import no.iktdev.japp.models.OperationResponse
import no.iktdev.japp.service.OperationsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/operations")
class OperationsController(
    private val operations: OperationsService
) {

    @PostMapping("/pause")
    suspend fun pause(@RequestBody body: OperationRequest): ResponseEntity<OperationResponse> {
        val response = operations.pause(body.duration, body.backupPath)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/resume")
    suspend fun resume(): ResponseEntity<OperationResponse> {
        val response = operations.resume()
        return ResponseEntity.ok(response)
    }
}
