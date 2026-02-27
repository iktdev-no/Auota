package no.iktdev.auota.crypt.encrypt.operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.auota.crypt.backend.BackendPaths
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator

class VerifyOperation(private val paths: BackendPaths) {

    private val log = KotlinLogging.logger {}

    /**
     * Verifiserer at kryptering fungerer på reverse-mount:
     * - skriver til backend (plaintext)
     * - sjekker at mount (encrypted view) viser kryptert innhold
     */
    suspend fun verify(): Boolean {
        val testContent = "AUOTA_TEST_${System.currentTimeMillis()}"
        val backendTestFile = paths.backend.resolve("auota-test.txt")
        val mountViewFile = paths.mount.resolve("auota-test.txt")

        log.info { "Starter verifisering av mount" }
        log.info { "Backend (writeable) : ${paths.backend}" }
        log.info { "Mount (read-only)  : ${paths.mount}" }

        // -------------------------------------------------
        // 1️⃣ Skriv testfil til backend (plaintext)
        // -------------------------------------------------
        try {
            withContext(Dispatchers.IO) {
                Files.writeString(backendTestFile, testContent)
            }
            log.info { "✅ Testfil skrevet til backend: $backendTestFile" }
        } catch (e: Exception) {
            log.error(e) { "❌ Klarte ikke å skrive testfil til backend" }
            return false
        }

        // -------------------------------------------------
        // 2️⃣ Sjekk at filen finnes kryptert i mount
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
            return false
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
        log.info { "FileStore-type for mount: $storeType → ser ut som FUSE? $looksLikeFuse" }

        // -------------------------------------------------
        // 4️⃣ Sjekk /proc/self/mounts
        // -------------------------------------------------
        val mountEntry = try {
            Files.readAllLines(Path.of("/proc/self/mounts"))
                .firstOrNull { line -> line.split(" ").getOrNull(1) == paths.mount.toAbsolutePath().toString() }
        } catch (e: Exception) {
            log.warn(e) { "Kunne ikke lese /proc/self/mounts" }
            null
        }

        if (mountEntry != null) {
            log.info { "Fant mount entry i /proc/self/mounts: $mountEntry" }
        } else {
            log.warn { "Fant IKKE mount entry i /proc/self/mounts for mount" }
        }

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
        if (ok) {
            log.info { "🟢 Verifisering OK: Kryptert mount fungerer som forventet" }
        } else {
            log.error { "🔴 Verifisering FEILET: Se logg for detaljer" }
        }

        return ok
    }
}