package no.iktdev.auota.crypt.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.auota.cli.RunCli
import no.iktdev.auota.crypt.backend.BackendPaths
import no.iktdev.auota.crypt.info.CryptInfo
import no.iktdev.auota.crypt.info.CryptInfoStore
import no.iktdev.auota.models.CryptConfig
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.UUID

abstract class InitOperationBase(
    protected val runCli: RunCli,
    protected val paths: BackendPaths,
    protected val infoStore: CryptInfoStore,
    protected val configDir: Path
) {

    val configFile = configDir.resolve("gocryptfs.conf")
    protected val log = KotlinLogging.logger {}

    suspend fun init(cfg: CryptConfig): Boolean {
        log.info { "Starting gocryptfs init…" }

        if (cfg.password.isNullOrBlank()) {
            log.error { "Init failed: password is blank" }
            return false
        }
        if (!prepareConfigDirectory()) return false

        if (!Files.exists(configFile)) {
            log.info { "No gocryptfs.conf found → init kryptert backend med config i $configFile" }

            val passFile = createTempPassFile(cfg.password) ?: return false

            val initArgs = buildInitArgs(passFile)

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

        return saveMetadata()
    }

    protected abstract fun buildInitArgs(passFile: Path): List<String>


    protected suspend fun prepareConfigDirectory(): Boolean {
        return try {
            withContext(Dispatchers.IO) { Files.createDirectories(configDir) }
            true
        } catch (e: Exception) {
            log.error(e) { "Failed to create config directory: $configDir" }
            false
        }
    }

    private suspend fun createTempPassFile(password: String): Path? = try {
        withContext(Dispatchers.IO) {
            val file = Files.createTempFile("gocryptfs-pass", ".txt")
            Files.writeString(file, password)
            file
        }
    } catch (e: Exception) {
        log.error(e) { "Failed to create temporary password file" }
        null
    }

    private fun saveMetadata(): Boolean {
        val info = try {
            CryptInfo(
                backendId = UUID.randomUUID().toString(),
                created = System.currentTimeMillis(),
                cryptHash = calculateBackendHash()
            )
        } catch (e: Exception) {
            log.error(e) { "Failed to calculate backend hash" }
            return false
        }

        return try {
            infoStore.saveConfigInfo(info)
            infoStore.saveBackendInfo(info)
            log.info { "Init complete. BackendId=${info.backendId}" }
            true
        } catch (e: Exception) {
            log.error(e) { "Failed to write encryption metadata files" }
            false
        }
    }

    protected fun calculateBackendHash(): String {
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