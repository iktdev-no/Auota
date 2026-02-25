package no.iktdev.japp.encrypt.info

data class EncryptionInfo(
    val backendId: String,
    val created: Long,
    val cryptHash: String,
    val version: Int = 1
)
