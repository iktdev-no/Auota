package no.iktdev.japp.service

import no.iktdev.japp.encrypt.EncryptionManager
import no.iktdev.japp.models.file.*
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths

@Service
class ExplorerService(
    private val encryption: EncryptionManager,
    private val backupRoots: List<String>,
    private val excludedPaths: List<String>
) {

    fun listAt(path: String): List<IFile> {
        val dir = File(path)

        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }

        return dir.listFiles()
            ?.map { file -> file.toFileInfo() }
            ?: emptyList()
    }

    fun pathToFile(path: String): IFile? {
        val file = File(path)
        if (!file.exists()) return null
        return file.toFileInfo()
    }

    private fun File.toFileInfo(): IFile {
        val filePath = this.toPath()

        val isExcluded = excludedPaths.any { filePath.startsWith(Paths.get(it)) }
        val isIncluded = backupRoots.any { filePath.startsWith(Paths.get(it)) }

        val isEncrypted = filePath.startsWith(encryption.encryptedDataPath)
        val isBackend = filePath.startsWith(encryption.dataPath)

        val fileActions = buildActions(isIncluded, isExcluded, this.isDirectory)

        return if (this.isDirectory) {
            Folder(
                name = this.name,
                uri = this.absolutePath,
                created = this.lastModified(),
                actions = fileActions,
                isInBackup = isIncluded,
                isExcludedFromBackup = isExcluded,
                isEncrypted = isEncrypted,
                isDataSource = isBackend
            )
        } else {
            File(
                name = this.name,
                uri = this.absolutePath,
                created = this.lastModified(),
                extension = this.extension,
                actions = fileActions,
                size = this.length(),
                isInBackup = isIncluded,
                isExcludedFromBackup = isExcluded,
                isEncrypted = isEncrypted,
                isDataSource = isBackend
            )
        }
    }


    private fun buildActions(
        isIncluded: Boolean,
        isExcluded: Boolean,
        isFolder: Boolean
    ): List<FileAction> {

        val fileActions = mutableListOf<FileAction>()

        if (isIncluded) {
            fileActions += FileAction(FileActionType.RemoveFromBackup)
            fileActions += FileAction(FileActionType.ExcludeFromBackup)
        } else {
            fileActions += FileAction(FileActionType.AddToBackup)
        }

        if (isExcluded) {
            fileActions += FileAction(FileActionType.IncludeInBackup)
        }

        return fileActions
    }
}

