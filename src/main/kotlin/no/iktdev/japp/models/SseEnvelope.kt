package no.iktdev.japp.models

data class SseEnvelope(
    val type: String,
    val payload: Any?
)
