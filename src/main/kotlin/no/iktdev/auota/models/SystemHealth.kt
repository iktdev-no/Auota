package no.iktdev.auota.models

import no.iktdev.auota.models.crypt.EncryptionState

data class SystemHealth(
    val auth: AuthStatus,
    val jottad: JottaDaemonState,
    val lastUpdated: Long
)

enum class AuthStatus {
    LOGGED_IN,
    LOGGED_OUT,
    UNKNOWN
}
