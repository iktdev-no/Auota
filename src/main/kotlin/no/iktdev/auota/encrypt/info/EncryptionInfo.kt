package no.iktdev.auota.encrypt.info

data class EncryptionInfo(
    val backendId: String,
    val created: Long,
    val cryptHash: String,
    val version: Int = 1
)
