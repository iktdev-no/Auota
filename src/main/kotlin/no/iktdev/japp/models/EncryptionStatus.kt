package no.iktdev.japp.models

data class EncryptionStatus(
    val enabled: Boolean,
    val mounted: Boolean,
    val backendExists: Boolean,
    val algorithm: String?,
    val reason: String?
)
