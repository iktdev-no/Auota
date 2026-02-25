package no.iktdev.auota.models

data class EncryptionConfig(
    val enabled: Boolean = false,
    val algorithm: String = "AES-GCM-256",
    val password: String? = null,
)