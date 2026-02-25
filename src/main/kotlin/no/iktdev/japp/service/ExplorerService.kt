package no.iktdev.japp.service

import no.iktdev.japp.backup.BackupConfigStore
import no.iktdev.japp.encrypt.EncryptionManager
import no.iktdev.japp.models.file.*
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

@Service
class ExplorerService(
    private val encryption: EncryptionManager,
    private val backupConfigStore: BackupConfigStore
) {

    // Systemmapper som aldri skal vises i UI
    private val systemHiddenPaths = listOf(
        "/bin",
        "/boot",
        "/dev",
        "/etc",
        "/lib",
        "/lib64",
        "/opt",
        "/proc",
        "/root",
        "/sbin",
        "/srv",
        "/sys",
        "/tmp",
        "/var",
        "/usr"
    )

    fun listAt(path: String): List<IFile> {
        val dir = File(path)

        if (!dir.exists() || !dir.isDirectory) return emptyList()

        val cfg = backupConfigStore.load()

        return dir.listFiles()
            ?.filterNot { file ->
                val p = file.toPath()
                // bare skjul systemmapper, ikke ekskluderte filer
                systemHiddenPaths.any { p.startsWith(Paths.get(it)) }
            }
            ?.map { file -> file.toFileInfo(cfg) }
            ?: emptyList()
    }


    fun pathToFile(path: String): IFile? {
        val file = File(path)
        if (!file.exists()) return null

        val cfg = backupConfigStore.load()
        return file.toFileInfo(cfg)
    }

    private fun File.toFileInfo(cfg: no.iktdev.japp.backup.BackupConfig): IFile {
        val filePath: Path = this.toPath()

        // Sjekk om filen ligger under noen root
        val isIncluded = cfg.roots.any { root -> filePath.startsWith(Paths.get(root)) }

        // Sjekk om filen ligger under noen excludePaths tilhørende en root
        val isExcluded = cfg.excluded.any { item ->
            item.excludePaths.any { excludePath ->
                filePath.startsWith(Paths.get(excludePath))
            }
        }

        val isEncrypted = filePath.startsWith(encryption.encryptedDataPath)
        val isBackend = filePath.startsWith(encryption.dataPath)

        val fileActions = buildActions(
            isIncluded = isIncluded,
            isExcluded = isExcluded,
            isFolder = this.isDirectory,
            filePath = filePath
        )


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

    fun canBeAddedToBackup(path: Path): Boolean {
        val p = path.normalize()

        // 1) Ikke tillat systemmapper
        if (systemHiddenPaths.any { p.startsWith(Paths.get(it)) }) return false

        // 2) Ikke tillat root
        if (p.toString() == "/") return false

        // 3) Tillat backend og alt under
        if (p.startsWith(encryption.encryptedDataPath)) return true

        // 4) Tillat mount og alt under, men ikke selve mount-root
        if (p.startsWith(encryption.dataPath) && p != encryption.dataPath) return true

        // 5) Alt annet er ulovlig
        return false
    }

    private fun buildActions(
        isIncluded: Boolean,
        isExcluded: Boolean,
        isFolder: Boolean,
        filePath: Path
    ): List<FileAction> {

        val actions = mutableListOf<FileAction>()
        val canAdd = if (isFolder) canBeAddedToBackup(filePath) else false

        if (isIncluded) {
            actions += FileAction(FileActionType.RemoveFromBackup)
            actions += FileAction(FileActionType.ExcludeFromBackup)
        } else if (canAdd) {
            actions += FileAction(FileActionType.AddToBackup)
        }

        if (isExcluded) {
            actions += FileAction(FileActionType.IncludeInBackup)
        }

        return actions
    }



}
