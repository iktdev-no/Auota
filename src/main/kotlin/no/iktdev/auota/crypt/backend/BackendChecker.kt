package no.iktdev.auota.crypt.backend

import java.nio.file.Files

class BackendChecker(private val paths: BackendPaths) {

    /** Finnes backend-folderen (host-folderen med innhold)? */
    fun backendExists(): Boolean = Files.isDirectory(paths.backend)

    /** Er kryptert mount faktisk montert? */
    fun isMounted(): Boolean = try {
        Files.getFileStore(paths.mount).type().lowercase().contains("fuse")
    } catch (e: Exception) {
        false
    }

    /** Sjekk om backend (host-folder) har filer */
    fun backendHasFiles(): Boolean {
        if (!Files.exists(paths.backend)) return false

        Files.list(paths.backend).use { stream ->
            return stream
                .filter { Files.isRegularFile(it) }
                .anyMatch { file ->
                    val name = file.fileName.toString()
                    name != "gocryptfs.diriv"
                }
        }
    }
}