package no.iktdev.auota.encrypt.backend

import java.nio.file.Files

class BackendChecker(private val paths: BackendPaths) {

    fun backendExists(): Boolean = Files.isDirectory(paths.backend)

    fun isMounted(): Boolean = try {
        Files.getFileStore(paths.mount).type().lowercase().contains("fuse")
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

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
