package no.iktdev.auota.models

data class JottadStatus(
    val alive: Boolean,
    val state: JottaDaemonState,
    val pid: Long,
    val timestamp: Long
)