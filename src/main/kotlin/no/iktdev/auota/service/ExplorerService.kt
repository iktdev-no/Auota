package no.iktdev.auota.service

import no.iktdev.auota.backup.BackupConfigStore
import no.iktdev.auota.crypt.encrypt.EncryptionManager
import no.iktdev.auota.models.crypt.EncryptionState
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
    private val encryption: EncryptionManager,
    private val backupConfigStore: BackupConfigStore
) {

    private val alternativeFolders = mapOf(
        "data" to "/data",
        "media" to "/media",
        "mount" to "/mount",
        "mnt" to "/mnt"
    )

    private val success = listOf(EncryptionState.READY, EncryptionState.MANUAL_OVERRIDE)

    suspend fun listRoots(): List<Roots> {
        val roots = mutableListOf<Roots>()

        // Upload (bruker alltid /upload i UI)
        roots += Roots(
            id = "upload",
            name = "Upload",
            type = if (encryption.state.value in success)
                RootType.UploadEncrypted
            else
                RootType.UploadUnencrypted,
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
        alternativeFolders.forEach { (id, folder) ->
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
                path = "/" // JottaFs root
            )
        }

        return roots
    }

    /**
     * Lokal filutforsker for ALLE lokale paths.
     * Upload håndteres automatisk av backend via encryption.paths.
     */
    fun listAt(path: String): List<IFile> {
        // Hvis path er root → returner kun definerte lokale roots
        if (path == "/") {
            val cfg = backupConfigStore.load()

            val folders: MutableList<IFile> = mutableListOf()
            (if(encryption.state.value in success) {
                encryption.paths.mount.toFile().toFileInfo(cfg)
            } else {
                encryption.paths.backend.toFile().toFileInfo(cfg)
            }).also { folders.add(it) }
            File("/download").toFileInfo(cfg)
                .also { folders.add(it) }
            alternativeFolders.mapValues { File(it.value).toFileInfo(cfg) }
                .also { folders.addAll(it.values) }

            return folders
        }

        // Ellers: vanlig lokal filutforsking
        val resolvedPath = resolveLocalPath(path)
        val dir = File(resolvedPath)

        if (!dir.exists() || !dir.isDirectory) return emptyList()

        val cfg = backupConfigStore.load()

        return dir.listFiles()
            ?.map { file -> file.toFileInfo(cfg) }
            ?: emptyList()
    }


    /**
     * Oversetter /upload til riktig fysisk mappe basert på kryptering.
     */
    private fun resolveLocalPath(path: String): String {
        return if (path.startsWith("/upload")) {
            val relative = path.removePrefix("/upload")
            val base = if (encryption.state.value in success)
                encryption.paths.mount
            else
                encryption.paths.backend

            base.resolve(relative).normalize().toString()
        } else {
            path
        }
    }

    fun pathToFile(path: String): IFile? {
        val resolved = resolveLocalPath(path)
        val file = File(resolved)
        if (!file.exists()) return null

        val cfg = backupConfigStore.load()
        return file.toFileInfo(cfg)
    }

    private fun File.toFileInfo(cfg: no.iktdev.auota.backup.BackupConfig): IFile {
        val filePath: Path = this.toPath()

        val isIncluded = cfg.roots.any { root -> filePath.startsWith(Paths.get(root)) }
        val isExcluded = cfg.excluded.any { item ->
            item.excludePaths.any { excludePath ->
                filePath.startsWith(Paths.get(excludePath))
            }
        }

        val isEncrypted = filePath.startsWith(encryption.paths.mount)
        val isBackend = filePath.startsWith(encryption.paths.backend)

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

        // Finn faktisk upload-rot (kryptert eller ukryptert)
        val uploadRoot = if (encryption.state.value in success)
            encryption.paths.mount.normalize()
        else
            encryption.paths.backend.normalize()

        // 1. Må være en mappe
        if (!Files.isDirectory(p)) return false

        // 2. Må ligge under upload-root
        if (!p.startsWith(uploadRoot)) return false

        // 3. Men ikke selve upload-root
        if (p == uploadRoot) return false

        return true
    }


    private fun buildActions(
        isIncluded: Boolean,
        isExcluded: Boolean,
        isFolder: Boolean,
        filePath: Path
    ): List<FileAction> {

        val actions = mutableListOf<FileAction>()
        val canAdd = if (isFolder) canBeAddedToBackup(filePath) else false

        // Backup-handling
        if (isIncluded) {
            actions += FileAction(FileActionType.RemoveFromBackup)
            actions += FileAction(FileActionType.ExcludeFromBackup)
        } else if (canAdd) {
            actions += FileAction(FileActionType.AddToBackup)
        }

        if (isExcluded) {
            actions += FileAction(FileActionType.IncludeInBackup)
        }

        // Upload-handling
        if (isUnderAlternativeFolder(filePath)) {
            actions += FileAction(FileActionType.Upload)
        }

        return actions
    }

    private fun isUnderAlternativeFolder(path: Path): Boolean {
        val normalized = path.toAbsolutePath().normalize().toString()

        return alternativeFolders.values.any { alt ->
            normalized.startsWith(alt)
        }
    }


}
