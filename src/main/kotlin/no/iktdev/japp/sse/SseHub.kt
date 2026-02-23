package no.iktdev.japp.sse

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

@Component
class SseHub {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val subscribers = mutableListOf<FluxSink<Any>>()  // send objects, not String

    private var onFirstSubscriber: (() -> Unit)? = null
    private var onNoSubscribers: (() -> Unit)? = null

    fun stream(): Flux<Any> {
        return Flux.create { sink ->
            val first: Boolean
            synchronized(subscribers) {
                first = subscribers.isEmpty()
                subscribers += sink
            }

            if (first) {
                onFirstSubscriber?.invoke()
            }

            sink.onDispose {
                val empty: Boolean
                synchronized(subscribers) {
                    subscribers.remove(sink)
                    empty = subscribers.isEmpty()
                }
                if (empty) {
                    onNoSubscribers?.invoke()
                }
            }
        }
    }

    fun sendEnvelope(type: String, payload: Any?) {
        val envelope = mapOf("type" to type, "payload" to payload)
        synchronized(subscribers) {
            subscribers.forEach { it.next(envelope) }  // send object, not JSON string
        }
    }

    fun setSubscriberCallbacks(
        onFirst: () -> Unit,
        onNone: () -> Unit
    ) {
        onFirstSubscriber = onFirst
        onNoSubscribers = onNone
    }
}