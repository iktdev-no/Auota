package no.iktdev.japp.controller

import no.iktdev.japp.sse.SseHub
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class SseController(
    private val hub: SseHub
) {

    @GetMapping("/api/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun events(): Flux<Any> = hub.stream()  // Flux<Any> istedenfor Flux<String>

}