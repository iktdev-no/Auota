package no.iktdev.auota.crypt.backend

import java.nio.file.Path

data class BackendPaths(
    /** Den eksisterende host-mappen med data */
    val backend: Path,

    /** Kryptert mount-point (FUSE), tom ved mount) */
    val mount: Path,

    /** gocryptfs config */
    val config: Path,

    /** Backend info json */
    val backendInfo: Path,

    /** Encryption info json */
    val configEncryptionInfo: Path
)