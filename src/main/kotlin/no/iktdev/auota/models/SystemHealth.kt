package no.iktdev.auota.models

import no.iktdev.auota.models.crypt.EncryptionState

data class SystemHealth(
    val encryption: EncryptionState,
    val auth: AuthStatus,
    val jottad: JottaDaemonState,
    val mounted: Boolean,
    val backendExists: Boolean,
    val lastUpdated: Long
)

enum class AuthStatus {
    LOGGED_IN,
    LOGGED_OUT,
    UNKNOWN
}
