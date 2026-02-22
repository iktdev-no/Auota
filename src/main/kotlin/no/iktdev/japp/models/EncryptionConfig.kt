package no.iktdev.japp.models

data class EncryptionConfig(
    val enabled: Boolean = false,
    val algorithm: String = "aes-gcm",
    val keySource: String = "password", // "password" | "keyfile"
    val password: String? = null,
    val keyFile: String? = null
)
