package no.iktdev.auota.service

import mu.KotlinLogging
import no.iktdev.auota.backup.BackupConfig
import no.iktdev.auota.backup.BackupConfigStore
import no.iktdev.auota.models.backup.BackupItem
import org.springframework.stereotype.Service

@Service
class BackupStateService(
    private val store: BackupConfigStore
) {
    private val log = KotlinLogging.logger {}

    @Volatile
    private var config: BackupConfig = store.load()

    fun getRoots(): List<String> = config.roots

    fun getExcluded(): List<BackupItem> = config.excluded

    fun addRoot(path: String) {
        if (path !in config.roots) {
            config = config.copy(roots = config.roots + path)
            store.save(config)
            log.info { "Added backup root: $path" }
        }
    }

    fun removeRoot(path: String) {
        if (path in config.roots) {
            config = config.copy(roots = config.roots - path)
            // Fjern eventuelle ekskluderinger for denne rooten
            config = config.copy(excluded = config.excluded.filter { it.path != path })
            store.save(config)
            log.info { "Removed backup root: $path" }
        }
    }

    fun addExcluded(root: String, path: String) {
        val existing = config.excluded.find { it.path == root }
        if (existing != null) {
            if (path !in existing.excludePaths) {
                val updated = existing.copy(excludePaths = existing.excludePaths + path)
                config = config.copy(
                    excluded = config.excluded.map { if (it.path == root) updated else it }
                )
                store.save(config)
                log.info { "Added excluded path: $path to root $root" }
            }
        } else {
            config = config.copy(
                excluded = config.excluded + BackupItem(path = root, excludePaths = listOf(path))
            )
            store.save(config)
            log.info { "Added excluded path: $path to new root $root" }
        }
    }

    fun removeExcluded(root: String, path: String) {
        val existing = config.excluded.find { it.path == root } ?: return
        if (path in existing.excludePaths) {
            val updatedPaths = existing.excludePaths - path
            val updatedExcluded = if (updatedPaths.isEmpty()) {
                config.excluded.filter { it.path != root }
            } else {
                config.excluded.map { if (it.path == root) existing.copy(excludePaths = updatedPaths) else it }
            }
            config = config.copy(excluded = updatedExcluded)
            store.save(config)
            log.info { "Removed excluded path: $path from root $root" }
        }
    }
}