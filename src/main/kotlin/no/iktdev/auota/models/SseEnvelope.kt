package no.iktdev.auota.models

data class SseEnvelope(
    val type: String,
    val payload: Any?
)
