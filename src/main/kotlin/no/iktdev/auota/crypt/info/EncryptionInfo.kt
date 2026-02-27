package no.iktdev.auota.crypt.info

data class EncryptionInfo(
    val backendId: String,
    val created: Long,
    val cryptHash: String,
    val version: Int = 1
)
