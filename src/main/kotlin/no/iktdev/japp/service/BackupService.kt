package no.iktdev.japp.service

import mu.KotlinLogging
import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.service.status.JottaStatusService
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.iktdev.japp.backup.BackupIgnoreRemover
import no.iktdev.japp.backup.sync.IgnoreSyncer
import no.iktdev.japp.backup.sync.RootSyncer
import no.iktdev.japp.models.backup.BackupIEUpdate
import no.iktdev.japp.models.backup.BackupItem

@Service
class BackupService(
    private val cli: JottaCli,
    private val jottaStatusService: JottaStatusService,
    private val state: BackupStateService,
    private val explorerService: ExplorerService
) {

    private val log = KotlinLogging.logger {}

    /**
     * Hindrer at flere sync kjører samtidig.
     */
    private val syncMutex = Mutex()

    private fun validatePath(path: String): Path {
        val p = Path.of(path)

        require(Files.exists(p)) { "Path does not exist: $path" }
        require(Files.isDirectory(p)) { "Path is not a directory: $path" }
        require(explorerService.canBeAddedToBackup(p)) {
            "Path cannot be added to backup: $path"
        }

        return p
    }

    fun getFolders(): List<BackupItem> {
        val roots = state.getRoots()
        val excludedItems = state.getExcluded() // List<BackupItem>

        return roots.map { root ->
            // Finn excludePaths for denne rooten
            val excludesForRoot = excludedItems.find { it.path == root }?.excludePaths ?: emptyList()
            BackupItem(
                path = root,
                excludePaths = excludesForRoot
            )
        }
    }

    suspend fun add(path: String) {
        val validated = validatePath(path)
        state.addRoot(validated.toString())
        syncWithJotta()
    }

    /**
     * OBS: Destruktiv operasjon.
     * Bør alltid kreve eksplisitt bekreftelse i UI.
     */
    suspend fun remove(path: String) {
        state.removeRoot(path)
        syncWithJotta()
    }

    suspend fun ignore(payload: BackupIEUpdate) {
        val root = payload.backupRoot ?: findRootForPath(payload.exclude)
        state.addExcluded(root, payload.exclude)
        syncWithJotta()
    }

    suspend fun unignore(payload: BackupIEUpdate) {
        val root = payload.backupRoot ?: findRootForPath(payload.exclude)
        state.removeExcluded(root, payload.exclude)
        syncWithJotta()
    }

    private fun findRootForPath(path: String): String {
        val roots = state.getRoots()
        return roots
            .filter { path.startsWith(it) }
            .maxByOrNull { it.length } // mest spesifikke root
            ?: throw IllegalArgumentException("Path $path does not belong to any configured backup root")
    }


    // nye komponenter
    private val ignoreRemover = BackupIgnoreRemover(cli)
    private val ignoreSyncer = IgnoreSyncer(cli, ignoreRemover)
    private val rootSyncer = RootSyncer(cli)

    suspend fun syncWithJotta() = syncMutex.withLock {
        log.info { "Syncing backup roots and ignores with Jotta…" }

        val expectedRoots = state.getRoots().toSet()
        val expectedExcluded = state.getExcluded().toSet()
        validateExcludesBelongToRoots(expectedRoots, expectedExcluded)

        val jottaStatus = jottaStatusService.getStatus().parsed
        val actualRoots = jottaStatus
            ?.Backup?.State?.Enabled?.Backups
            ?.mapNotNull { it.Path }
            ?.toSet() ?: emptySet()

        // root sync via RootSyncer
        rootSyncer.sync(expectedRoots, actualRoots)

        // ignore sync via IgnoreSyncer
        val rawIgnores = cli.run("ignores", "list").output
        expectedRoots.forEach { root ->
            val item = expectedExcluded.find { it.path == root }
            val excludePaths = item?.excludePaths?.toSet() ?: emptySet()
            ignoreSyncer.syncIgnoresForRoot(root, excludePaths, rawIgnores)
        }

        log.info { "Sync complete" }
    }

    /**
     * Sikrer at excludes faktisk ligger under en root.
     * Hindrer farlige patterns som "/" eller feilkonfig.
     */
    private fun validateExcludesBelongToRoots(
        roots: Set<String>,
        excludes: Set<BackupItem>
    ) {
        excludes.forEach { item ->
            item.excludePaths.forEach { exclude ->
                require(roots.any { root -> exclude.startsWith(root) }) {
                    "Exclude $exclude does not belong to any configured root"
                }
            }
        }
    }


    suspend fun status() = cli.run("status")
    suspend fun scan() = cli.run("scan")

    suspend fun pause(duration: String? = null) =
        if (duration != null) cli.run("pause", duration)
        else cli.run("pause")

    suspend fun resume() = cli.run("resume")
}