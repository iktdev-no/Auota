package no.iktdev.auota.models.files


data class JottaFs(
    val Folders: List<JottaFolder>? = null,
    val Files: List<JottaFile>? = null
)

sealed class JottaFsItem {
    abstract val Name: String
    abstract val Path: String?
    abstract var actions: List<FileAction>
    abstract val type: FileType
}

data class JottaFolder(
    override val Name: String,
    override val Path: String? = null,
    override var actions: List<FileAction> = emptyList(),
    override val type: FileType = FileType.Folder
): JottaFsItem() {}

data class JottaFile(
    override val Name: String,
    override val Path: String,
    val Checksum: String,
    val Size: Long = -1,
    val Modified: Long,
    override var actions: List<FileAction> = emptyList(),
    var extension: String = "FILE",
    override val type: FileType = FileType.File
): JottaFsItem()
