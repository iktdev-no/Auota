package no.iktdev.japp.controller

import no.iktdev.japp.sse.SseHub
import no.iktdev.japp.models.LogfileResponse
import no.iktdev.japp.service.LogService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/logs")
class LogController(
    private val logService: LogService,
    private val sse: SseHub
) {

    @GetMapping("/file")
    suspend fun getLogfile(): ResponseEntity<LogfileResponse> {
        val response = logService.getLogfile()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/pull")
    suspend fun pullLogfile(): ResponseEntity<String> {
        val content = logService.readLogfile()
        return if (content != null) {
            ResponseEntity.ok(content)
        } else {
            ResponseEntity.notFound().build()
        }
    }

}
