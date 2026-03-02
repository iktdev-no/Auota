package no.iktdev.auota.models.files

data class Roots(
    val id: String,
    val name: String,
    val type: RootType,
    val path: String
)


enum class RootType {
    Jotta,
    UploadUnencrypted,
    UploadEncrypted,
    Download,
    LocalFolder
}
