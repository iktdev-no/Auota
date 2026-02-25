package no.iktdev.japp.service

import mu.KotlinLogging
import no.iktdev.japp.backup.BackupConfig
import no.iktdev.japp.backup.BackupConfigStore
import org.springframework.stereotype.Service

@Service
class BackupStateService(
    private val store: BackupConfigStore
) {
    private val log = KotlinLogging.logger {}

    @Volatile
    private var config: BackupConfig = store.load()

    fun getRoots(): List<String> = config.roots
    fun getExcluded(): List<String> = config.excluded

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
            store.save(config)
            log.info { "Removed backup root: $path" }
        }
    }

    fun addExcluded(path: String) {
        if (path !in config.excluded) {
            config = config.copy(excluded = config.excluded + path)
            store.save(config)
            log.info { "Added excluded path: $path" }
        }
    }

    fun removeExcluded(path: String) {
        if (path in config.excluded) {
            config = config.copy(excluded = config.excluded - path)
            store.save(config)
            log.info { "Removed excluded path: $path" }
        }
    }
}
