package no.iktdev.auota.crypt.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogger
import mu.KotlinLogging
import no.iktdev.auota.crypt.backend.BackendPaths
import java.nio.file.Files
import java.nio.file.Path

abstract class VerifyOperationBase(
    protected val paths: BackendPaths
) {

    abstract val log: KLogger

    /** Subclass bestemmer testfilens navn og innhold */
    protected abstract fun prepareTestFile(): Pair<Path, String>

    /** Om mount er forventet read-only, suppress script warnings */
    protected open val suppressWritable: Boolean = false

    /** Verifiserer at kryptering fungerer */
    suspend fun verify(): Boolean {
        val (backendTestFile, testContent) = prepareTestFile()
        val mountViewFile = paths.mount.resolve(backendTestFile.fileName)

        log.info { "Starter verifisering av mount" }
        log.info { "Backend (writeable) : ${paths.backend}" }
        log.info { "Mount (view)        : ${paths.mount}" }

        // -------------------------------------------------
        // 1️⃣ Skriv testfil til backend
        // -------------------------------------------------
        try {
            withContext(Dispatchers.IO) { Files.writeString(backendTestFile, testContent) }
            log.info { "✅ Testfil skrevet til backend: $backendTestFile" }
        } catch (e: Exception) {
            log.error(e) { "❌ Klarte ikke å skrive testfil til backend" }
            return false
        }

        // -------------------------------------------------
        // 2️⃣ Les mount view
        // -------------------------------------------------
        val mountBytes = try {
            withContext(Dispatchers.IO) { Files.readAllBytes(mountViewFile) }
        } catch (e: Exception) {
            log.error(e) { "❌ Klarte ikke å lese testfil fra mount: $mountViewFile" }
            return false
        }

        val containsPlaintext = String(mountBytes).contains(testContent)
        if (containsPlaintext) {
            log.error { "❌ Mount inneholder plaintext → kryptering feilet" }
        } else {
            log.info { "✅ Fil vises kryptert i mount: $mountViewFile" }
        }

        // -------------------------------------------------
        // 3️⃣ Sjekk FileStore-type
        // -------------------------------------------------
        val storeType = try {
            withContext(Dispatchers.IO) { Files.getFileStore(paths.mount).type().lowercase() }
        } catch (e: Exception) {
            log.warn(e) { "Kunne ikke hente FileStore-type for mount" }
            "unknown"
        }
        val looksLikeFuse = storeType.contains("fuse")
        log.info { "FileStore-type for mount: $storeType (FUSE=${looksLikeFuse})" }

        // -------------------------------------------------
        // 4️⃣ Sjekk /proc/self/mounts
        // -------------------------------------------------
        val mountEntry = try {
            Files.readAllLines(Path.of("/proc/self/mounts"))
                .firstOrNull { it.split(" ").getOrNull(1) == paths.mount.toAbsolutePath().toString() }
        } catch (e: Exception) {
            log.warn(e) { "Kunne ikke lese /proc/self/mounts" }
            null
        }

        if (mountEntry != null) log.info { "Fant mount entry i /proc/self/mounts: $mountEntry" }
        else log.warn { "Fant IKKE mount entry i /proc/self/mounts for mount" }

        // -------------------------------------------------
        // 5️⃣ Rydd opp testfil
        // -------------------------------------------------
        try {
            withContext(Dispatchers.IO) { Files.deleteIfExists(backendTestFile) }
            log.info { "✅ Testfil fjernet fra backend" }
        } catch (e: Exception) {
            log.warn(e) { "Kunne ikke slette testfil fra backend" }
        }

        // -------------------------------------------------
        // 6️⃣ Konklusjon
        // -------------------------------------------------
        val ok = !containsPlaintext && mountEntry != null
        if (ok) log.info { "🟢 Verifisering OK: Kryptert mount fungerer som forventet" }
        else log.error { "🔴 Verifisering FEILET: Se logg for detaljer" }

        return ok
    }
}