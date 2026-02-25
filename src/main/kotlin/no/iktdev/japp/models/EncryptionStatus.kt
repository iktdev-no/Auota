package no.iktdev.japp.models

data class EncryptionStatus(
    val state: EncryptionState,
    val verified: Boolean,
    val enabled: Boolean,
    val passwordSet: Boolean,
    val passwordIncorrect: Boolean,
    val mounted: Boolean,
    val backendExists: Boolean,
    val algorithm: String?,
    val reason: String?,
    val exportable: Boolean,
)