package no.iktdev.japp.encrypt.backend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files

class BackendReset(private val paths: BackendPaths) {

    suspend fun resetConfig(): Boolean {
        if (Files.exists(paths.config)) {
            withContext(Dispatchers.IO) {
                Files.delete(paths.config)
            }
        }
        return true
    }
}
