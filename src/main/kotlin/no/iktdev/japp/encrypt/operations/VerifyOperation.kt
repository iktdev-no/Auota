package no.iktdev.japp.encrypt.operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.japp.encrypt.backend.BackendPaths
import java.nio.file.Files
import java.util.Comparator

class VerifyOperation(private val paths: BackendPaths) {

    private val log = KotlinLogging.logger {}

    suspend fun verify(): Boolean {
        val testContent = "AUOTA_TEST_${System.currentTimeMillis()}"
        val testFile = paths.mount.resolve("auota-test.txt")

        // 1. Write test file
        try {
            withContext(Dispatchers.IO) {
                Files.writeString(testFile, testContent)
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to write test file to mount: $testFile" }
            return false
        }

        // 2. Find newest backend file
        val backendFile = try {
            withContext(Dispatchers.IO) {
                Files.walk(paths.backend)
            }.use { stream ->
                stream.filter { Files.isRegularFile(it) }
                    .max(Comparator.comparingLong { Files.getLastModifiedTime(it).toMillis() })
                    .orElse(null)
            }
        } catch (e: Exception) {
            log.error(e) { "Failed walking backend directory: ${paths.backend}" }
            return false
        }

        if (backendFile == null) {
            log.error { "Verify failed: backend has no files after writing test file" }
            return false
        }

        // 3. Read backend file
        val backendBytes = try {
            withContext(Dispatchers.IO) {
                Files.readAllBytes(backendFile)
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to read backend file: $backendFile" }
            return false
        }

        // 4. Backend must NOT contain plaintext
        if (String(backendBytes).contains(testContent)) {
            log.error { "Verify failed: backend contains plaintext test content → encryption not working" }
            return false
        }

        // 5. Read back from mount
        val readBack = try {
            withContext(Dispatchers.IO) {
                Files.readString(testFile)
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to read test file back from mount: $testFile" }
            return false
        }

        // Cleanup
        try {
            withContext(Dispatchers.IO) {
                Files.deleteIfExists(testFile)
            }
        } catch (e: Exception) {
            log.warn(e) { "Failed to delete test file: $testFile" }
        }

        // 6. Compare
        val ok = readBack == testContent
        if (!ok) {
            log.error { "Verify failed: readBack != testContent" }
        }

        return ok
    }
}

