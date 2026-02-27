package no.iktdev.auota.models

import no.iktdev.auota.models.crypt.EncryptionState

data class EncryptionStatus(
    val state: EncryptionState,
    val verified: Boolean,
    val enabled: Boolean,
    val passwordSet: Boolean,
    val passwordIncorrect: Boolean,
    val mounted: Boolean,
    val manualOverride: Boolean,
    val backendExists: Boolean,
    val algorithm: String?,
    val reason: String?,
    val exportable: Boolean,
)