package no.iktdev.auota.models

data class LogfileResponse(
    val success: Boolean,
    val path: String?,
    val message: String? = null
)
