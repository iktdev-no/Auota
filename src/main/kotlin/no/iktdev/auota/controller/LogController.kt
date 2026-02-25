package no.iktdev.auota.controller

import kotlinx.coroutines.reactor.asFlux
import no.iktdev.auota.service.LogService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/logs")
class LogController(
    private val logService: LogService
) {

    @GetMapping("/file", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamFile(@RequestParam path: String): Flux<String> {
        return logService.streamFile(path).asFlux()
    }

    @GetMapping("/jotta", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun streamJotta(): Flux<String> {
        return logService.streamJottaLog().asFlux()
    }

    @GetMapping("/list", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listLogs(): List<String> {
        return logService.listAvailableLogs()
    }
}