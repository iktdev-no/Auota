package no.iktdev.auota.encrypt.backend

import java.nio.file.Path


data class BackendPaths(
    val backend: Path,
    val mount: Path,
    val config: Path,
    val backendInfo: Path,
    val configEncryptionInfo: Path
)
