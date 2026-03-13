package no.iktdev.auota.service

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.PosixFilePermission

@Service
class DecryptedSyncService {

    private val log = KotlinLogging.logger {}

    data class FileState(
        val size: Long,
        val lastModified: Long
    )

    // Hvor mange filer som kan eksporteres samtidig
    private val parallelism = 4
    private val exportSemaphore = Semaphore(parallelism)

    suspend fun startPolling(
        decryptedFolder: Path,
        exportFolder: Path,
        pollIntervalMs: Long = 500
    ) = withContext(Dispatchers.IO) {

        log.info { "🔍 Starter polling av decrypted view: $decryptedFolder (parallelism=$parallelism)" }

        var previousState = emptyMap<Path, FileState>()

        // Egen scope for eksportjobber
        val exportScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        while (true) {
            val currentState = scanDirectory(decryptedFolder)

            // Finn nye eller endrede filer
            val changedFiles = currentState.filter { (relativePath, state) ->
                val old = previousState[relativePath]
                old == null || old.size != state.size || old.lastModified != state.lastModified
            }

            for ((relativePath, _) in changedFiles) {
                val decryptedFile = decryptedFolder.resolve(relativePath)

                if (!Files.isRegularFile(decryptedFile)) continue

                // Vent til decrypted filen er ferdig skrevet
                waitUntilStable(decryptedFile)

                val target = exportFolder.resolve(relativePath)

                // Start eksport i parallell
                exportScope.launch {
                    exportSemaphore.withPermit {
                        try {
                            handleExport(decryptedFile, target, relativePath)
                        } catch (e: Exception) {
                            log.error(e) { "❌ Feil under eksport av $relativePath" }
                        }
                    }
                }
            }

            previousState = currentState
            delay(pollIntervalMs)
        }
    }

    private suspend fun handleExport(
        decryptedFile: Path,
        target: Path,
        relativePath: Path
    ) = withContext(Dispatchers.IO) {

        // Hvis target finnes, sjekk hash
        if (Files.exists(target)) {
            val srcHash = sha256(decryptedFile)
            val dstHash = sha256(target)

            if (srcHash == dstHash) {
                log.debug { "⏭️ Hopper over $relativePath, hash match" }
                return@withContext
            }

            log.info { "🔄 Hash mismatch for $relativePath, overskriver…" }
        }

        safeCopyWithRetries(
            source = decryptedFile,
            target = target
        )
    }

    private fun scanDirectory(root: Path): Map<Path, FileState> {
        val result = mutableMapOf<Path, FileState>()
        val stack = ArrayDeque<Path>()

        if (!Files.exists(root)) return result

        stack.add(root)

        while (stack.isNotEmpty()) {
            val dir = stack.removeFirst()

            val stream = try {
                Files.newDirectoryStream(dir)
            } catch (e: NoSuchFileException) {
                continue // katalog forsvant – ignorer
            } catch (e: Exception) {
                continue
            }

            stream.use {
                for (entry in it) {
                    try {
                        if (Files.isDirectory(entry)) {
                            stack.add(entry)
                        } else if (Files.isRegularFile(entry)) {
                            val relative = root.relativize(entry)
                            result[relative] = FileState(
                                size = Files.size(entry),
                                lastModified = Files.getLastModifiedTime(entry).toMillis()
                            )
                        }
                    } catch (_: NoSuchFileException) {
                        // fil forsvant – ignorer
                    } catch (_: Exception) {
                        // ignorer alt annet
                    }
                }
            }
        }

        return result
    }


    private fun waitUntilStable(path: Path) {
        var lastSize = -1L
        while (true) {
            val size = Files.size(path)
            if (size == lastSize) break
            lastSize = size
            Thread.sleep(200)
        }
    }

    private fun sha256(path: Path): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun safeCopyWithRetries(
        source: Path,
        target: Path,
        maxAttempts: Int = 5,
        initialDelayMs: Long = 50
    ) {
        var attempt = 1
        var delay = initialDelayMs

        while (true) {
            try {
                // Sørg for at katalogen finnes
                Files.createDirectories(target.parent)

                // Kopier filen
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)

                // Juster permissions hvis POSIX
                val store = Files.getFileStore(target)
                if (store.supportsFileAttributeView("posix")) {
                    // Katalog: rwxrwxr-x
                    Files.setPosixFilePermissions(
                        target.parent,
                        setOf(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.GROUP_READ,
                            PosixFilePermission.GROUP_WRITE,
                            PosixFilePermission.GROUP_EXECUTE,
                            PosixFilePermission.OTHERS_READ,
                            PosixFilePermission.OTHERS_EXECUTE
                        )
                    )
                    // Fil: rw-rw-r--
                    Files.setPosixFilePermissions(
                        target,
                        setOf(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.GROUP_READ,
                            PosixFilePermission.GROUP_WRITE,
                            PosixFilePermission.OTHERS_READ
                        )
                    )
                }

                log.info { "📤 Kopiert ut: ${source.fileName} (forsøk $attempt)" }
                return

            } catch (e: Exception) {
                if (attempt >= maxAttempts) {
                    log.error(e) {
                        "❌ Klarte ikke å kopiere ${source.fileName} etter $attempt forsøk"
                    }
                    return
                }

                log.warn {
                    "⚠️  Kopiering feilet for ${source.fileName}, prøver igjen om ${delay}ms (forsøk $attempt)"
                }

                Thread.sleep(delay)
                delay *= 2
                attempt++
            }
        }
    }

}
