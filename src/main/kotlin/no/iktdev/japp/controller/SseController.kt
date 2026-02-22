package no.iktdev.japp.controller

import no.iktdev.japp.SseHub
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class SseController(
    private val hub: SseHub
) {

    @GetMapping("/api/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun events(): Flux<String> = hub.stream()
}
