package no.iktdev.auota.models.jottaFs

import no.iktdev.auota.TSGenOverride
import no.iktdev.auota.models.file.FileType


data class JottaFs(
    val Folders: List<JottaFolder>? = null,
    val Files: List<JottaFile>? = null
)

sealed class JottaFsItem {
    abstract val Name: String
    abstract val Path: String?
    abstract var actions: List<JottaFileAction>
    abstract val type: FileType
}

data class JottaFolder(
    override val Name: String,
    override val Path: String? = null,
    override var actions: List<JottaFileAction> = emptyList(),
    override val type: FileType = FileType.Folder
): JottaFsItem() {}

data class JottaFile(
    override val Name: String,
    override val Path: String,
    val Checksum: String,
    val Size: Long = -1,
    val Modified: Long,
    override var actions: List<JottaFileAction> = emptyList(),
    var extension: String = "FILE",
    override val type: FileType = FileType.File
): JottaFsItem()

enum class JottaFileActionType(val label: String) {
    Download("Download"),
    Open("Open")
}

data class JottaFileAction(
    val id: JottaFileActionType,
    val title: String = id.label,
    val requiresConfirmation: Boolean = false
)