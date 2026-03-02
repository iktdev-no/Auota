package no.iktdev.auota.models.files

enum class FileType {
    Folder,
    File
}

enum class FileIcon {
    Default,
    Encrypted,
    Backend,
    BackupIncluded,
    BackupExcluded
}

sealed class IFile {
    abstract val name: String
    abstract val uri: String
    abstract val created: Long
    abstract val type: FileType
    abstract val actions: List<FileAction>

    abstract val isDataSource: Boolean
    abstract val isInBackup: Boolean
    abstract val isExcludedFromBackup: Boolean
    abstract val isEncrypted: Boolean
    abstract val icon: FileIcon
}

data class File(
    override val name: String,
    override val uri: String,
    override val created: Long,
    val extension: String,
    override val actions: List<FileAction>,
    val size: Long,
    override val isInBackup: Boolean = false,
    override val isExcludedFromBackup: Boolean = false,
    override val isEncrypted: Boolean = false,
    override val isDataSource: Boolean
) : IFile() {

    override val type = FileType.File

    override val icon: FileIcon =
        when {
            isDataSource -> FileIcon.Backend
            isEncrypted -> FileIcon.Encrypted
            isExcludedFromBackup -> FileIcon.BackupExcluded
            isInBackup -> FileIcon.BackupIncluded
            else -> FileIcon.Default
        }

}

data class Folder(
    override val name: String,
    override val uri: String,
    override val created: Long,
    override val actions: List<FileAction>,
    override val isInBackup: Boolean = false,
    override val isExcludedFromBackup: Boolean = false,
    override val isEncrypted: Boolean = false,
    override val isDataSource: Boolean
) : IFile() {

    override val type = FileType.Folder

    override val icon: FileIcon =
        when {
            isDataSource -> FileIcon.Backend
            isEncrypted -> FileIcon.Encrypted
            isExcludedFromBackup -> FileIcon.BackupExcluded
            isInBackup -> FileIcon.BackupIncluded
            else -> FileIcon.Default
        }

}


enum class FileActionType(val label: String) {
    AddToBackup("Add to backup"),
    IncludeInBackup("Include in backup"),
    ExcludeFromBackup("Exclude from backup"),
    RemoveFromBackup("Remove from backup"),
    Upload("Upload"),
    Download("Download"),
    Open("Open"),
}

data class FileAction(
    val id: FileActionType,
    val title: String = id.label,
    val requiresConfirmation: Boolean = false
)

