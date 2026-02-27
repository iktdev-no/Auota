package no.iktdev.auota.crypt.encrypt.operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.info.EncryptionInfo
import no.iktdev.auota.crypt.info.EncryptionInfoStore
import no.iktdev.auota.models.EncryptionConfig
import java.nio.file.Files
import java.security.MessageDigest
import java.util.UUID
import java.nio.file.Path

class InitOperation(
    private val runCli: RunCli,
    private val paths: BackendPaths,
    private val infoStore: EncryptionInfoStore,
    private val configDir: Path // Nytt: /config
) {

    private val log = KotlinLogging.logger {}

    suspend fun init(cfg: EncryptionConfig): Boolean {
        log.info { "Starting gocryptfs init…" }

        if (cfg.password.isNullOrBlank()) {
            log.error { "Init failed: password is blank" }
            return false
        }

        // Sørg for at config-mappen finnes
        try {
            withContext(Dispatchers.IO) { Files.createDirectories(configDir) }
        } catch (e: Exception) {
            log.error(e) { "Failed to create config directory: $configDir" }
            return false
        }

        val configFile = configDir.resolve("gocryptfs.conf")

        // Init kun hvis config ikke finnes
        if (!Files.exists(configFile)) {
            log.info { "No gocryptfs.conf found → init kryptert backend (reverse) med config i $configFile" }

            val passFile = try {
                withContext(Dispatchers.IO) {
                    val file = Files.createTempFile("gocryptfs-pass", ".txt")
                    Files.writeString(file, cfg.password)
                    file
                }
            } catch (e: Exception) {
                log.error(e) { "Failed to create temporary password file" }
                return false
            }

            val initArgs = listOf(
                "-init",
                "-reverse",
                "-plaintextnames",
                "-config", configFile.toString(),
                "-passfile", passFile.toString(),
                paths.backend.toString()   // /upload
            )

            val result = runCli.runCommand("gocryptfs", initArgs)
            passFile.toFile().delete()

            if (result.resultCode != 0) {
                log.error { "gocryptfs init failed (exit=${result.resultCode}). Output: ${result.output}" }
                return false
            }

            log.info { "gocryptfs init succeeded" }
        } else {
            log.info { "gocryptfs.conf allerede finnes → hopper over init" }
        }

        // Skriv metadata
        val info = try {
            EncryptionInfo(
                backendId = UUID.randomUUID().toString(),
                created = System.currentTimeMillis(),
                cryptHash = calculateBackendHash()
            )
        } catch (e: Exception) {
            log.error(e) { "Failed to calculate backend hash" }
            return false
        }

        try {
            infoStore.saveConfigInfo(info)
            infoStore.saveBackendInfo(info)
        } catch (e: Exception) {
            log.error(e) { "Failed to write encryption metadata files" }
            return false
        }

        log.info { "Init complete. BackendId=${info.backendId}" }
        return true
    }

    private fun calculateBackendHash(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.walk(paths.backend).use { stream ->
            stream.filter { Files.isRegularFile(it) }
                .sorted()
                .forEach { file ->
                    digest.update(Files.readAllBytes(file))
                }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}