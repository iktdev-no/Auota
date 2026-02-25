package no.iktdev.japp.sse

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Component
class SseHub {

    // Replay last event to new subscribers (hot stream)
    private val sink = Sinks.many().replay().latest<Any>()

    fun sendEnvelope(type: String, payload: Any?) {
        val envelope = mapOf(
            "type" to type,
            "payload" to payload
        )
        sink.tryEmitNext(envelope)
    }

    fun stream(): Flux<Any> = sink.asFlux()
}
