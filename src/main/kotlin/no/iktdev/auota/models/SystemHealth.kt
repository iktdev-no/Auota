package no.iktdev.auota.models

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
