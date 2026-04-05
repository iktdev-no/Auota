package no.iktdev.auota.service

import no.iktdev.auota.backup.BackupConfigStore
import no.iktdev.auota.models.files.*
import no.iktdev.auota.service.status.JottaStatusService
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class ExplorerService(
    private val jottaStatusService: JottaStatusService,
    private val backupConfigStore: BackupConfigStore
) {

    private val alternativeFolders: Map<String, String> = mapOf(
        "data" to "/data",
        "media" to "/media",
        "mount" to "/mount",
        "mnt" to "/mnt"
    )

    suspend fun listRoots(): List<Roots> {
        val roots: MutableList<Roots> = mutableListOf()

        // Upload
        roots += Roots(
            id = "upload",
            name = "Upload",
            type = RootType.Upload,
            path = "/upload"
        )

        // Download
        roots += Roots(
            id = "download",
            name = "Download",
            type = RootType.Download,
            path = "/download"
        )

        // Alternative mapper
        alternativeFolders.forEach { (id: String, folder: String) ->
            val f = File(folder)
            if (f.exists() && f.isDirectory) {
                roots += Roots(
                    id = id,
                    name = f.name,
                    type = RootType.LocalFolder,
                    path = folder
                )
            }
        }

        // Jottacloud
        if (jottaStatusService.getStatus().success) {
            roots += Roots(
                id = "jotta",
                name = "Jotta Cloud",
                type = RootType.Jotta,
                path = "/"
            )
        }

        return roots
    }

    fun listAt(path: String): List<IFile> {
        if (path == "/") {
            val cfg = backupConfigStore.load()

            val folders: MutableList<IFile> = mutableListOf()
            File("/upload").toFileInfo(cfg).also { folders.add(it) }
            File("/download").toFileInfo(cfg).also { folders.add(it) }

            alternativeFolders.values
                .map { folder -> File(folder).toFileInfo(cfg) }
                .also { folders.addAll(it) }

            return folders
        }

        val resolvedPath: String = path
        val dir = File(resolvedPath)

        if (!dir.exists() || !dir.isDirectory) return emptyList()

        val cfg = backupConfigStore.load()

        return dir.listFiles()
            ?.map { file -> file.toFileInfo(cfg) }
            ?: emptyList()
    }

    fun pathToFile(path: String): IFile? {
        val file = File(path)
        if (!file.exists()) return null

        val cfg = backupConfigStore.load()
        return file.toFileInfo(cfg)
    }

    private fun File.toFileInfo(cfg: no.iktdev.auota.backup.BackupConfig): IFile {
        val filePath: Path = this.toPath()

        val isIncluded: Boolean = cfg.roots.any { root ->
            filePath.startsWith(Paths.get(root))
        }

        val isExcluded: Boolean = cfg.excluded.any { item ->
            item.excludePaths.any { excludePath ->
                filePath.startsWith(Paths.get(excludePath))
            }
        }

        val fileActions: List<FileAction> = buildActions(
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
                isEncrypted = false,
                isDataSource = false
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
                isEncrypted = false,
                isDataSource = false
            )
        }
    }

    fun canBeAddedToBackup(path: Path): Boolean {
        val p: Path = path.normalize()

        // Nå er upload alltid /upload
        val uploadRoot: Path = Paths.get("/upload").normalize()

        if (!Files.isDirectory(p)) return false
        if (!p.startsWith(uploadRoot)) return false
        if (p == uploadRoot) return false

        return true
    }

    private fun buildActions(
        isIncluded: Boolean,
        isExcluded: Boolean,
        isFolder: Boolean,
        filePath: Path
    ): List<FileAction> {

        val actions: MutableList<FileAction> = mutableListOf()
        val canAdd: Boolean = if (isFolder) canBeAddedToBackup(filePath) else false

        if (isIncluded) {
            actions += FileAction(FileActionType.RemoveFromBackup)
            actions += FileAction(FileActionType.ExcludeFromBackup)
        } else if (canAdd) {
            actions += FileAction(FileActionType.AddToBackup)
        }

        if (isExcluded) {
            actions += FileAction(FileActionType.IncludeInBackup)
        }

        if (isUnderAlternativeFolder(filePath)) {
            actions += FileAction(FileActionType.Upload)
        }

        return actions
    }

    private fun isUnderAlternativeFolder(path: Path): Boolean {
        val normalized: String = path.toAbsolutePath().normalize().toString()
        return alternativeFolders.values.any { alt ->
            normalized.startsWith(alt)
        }
    }
}
