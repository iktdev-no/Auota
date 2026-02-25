package no.iktdev.japp.encrypt.operations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.japp.cli.RunCli
import no.iktdev.japp.encrypt.backend.BackendPaths
import no.iktdev.japp.encrypt.info.EncryptionInfo
import no.iktdev.japp.encrypt.info.EncryptionInfoStore
import no.iktdev.japp.models.EncryptionConfig
import java.nio.file.Files
import java.security.MessageDigest
import java.util.UUID

class InitOperation(
    private val runCli: RunCli,
    private val paths: BackendPaths,
    private val infoStore: EncryptionInfoStore
) {

    private val log = KotlinLogging.logger {}

    suspend fun init(cfg: EncryptionConfig): Boolean {
        log.info { "Starting gocryptfs init…" }

        if (cfg.password.isNullOrBlank()) {
            log.error { "Init failed: password is blank" }
            return false
        }

        val passFile = try {
            withContext(Dispatchers.IO) {
                Files.createDirectories(paths.backend)
                Files.createTempFile("gocryptfs-pass", ".txt").also {
                    Files.writeString(it, cfg.password)
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to create temporary password file" }
            return false
        }

        log.info { "Running gocryptfs -init on backend: ${paths.backend}" }

        val result = runCli.runCommand(
            "gocryptfs",
            listOf(
                "-init",
                "-plaintextnames",
                "-passfile",
                passFile.toString(),
                paths.backend.toString())
        )

        passFile.toFile().delete()

        if (result.resultCode != 0) {
            log.error {
                "gocryptfs init failed (exit=${result.resultCode}). Output: ${result.output}"
            }
            return false
        }

        log.info { "gocryptfs init succeeded. Writing metadata…" }

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
