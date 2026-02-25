package no.iktdev.japp.models

data class JottaVersionInfo(
    val jottadExecutable: String,
    val jottadAppdata: String,
    val jottadLogfile: String,
    val jottadVersion: String,
    val remoteVersion: String,
    val jottaCliVersion: String,
    val releaseNotes: String
)
