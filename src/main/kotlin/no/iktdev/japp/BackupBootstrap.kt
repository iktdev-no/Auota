package no.iktdev.japp

import kotlinx.coroutines.runBlocking
import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.service.StatusService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BackupBootstrap(
    private val cli: JottaCli,
    private val statusService: StatusService,
    private val sse: SseHub,
    @Value("\${backup.root}") private val backupRoot: String
) {

    private val log = LoggerFactory.getLogger(BackupBootstrap::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun onReady() = runBlocking {
        log.info("BackupBootstrap: sjekker backup-status...")

        val status = statusService.getStatus()

        if (!status.success || status.parsed == null) {
            log.error("BackupBootstrap: kunne ikke hente status: ${status.raw}")
            return@runBlocking
        }

        val backups = status.parsed.Backup
            ?.State
            ?.Enabled
            ?.Backups
            ?: emptyList()

        val alreadyAdded = backups.any { it.Path == backupRoot }

        if (alreadyAdded) {
            log.info("Backup-root '$backupRoot' er allerede registrert.")
            sse.sendEnvelope("bootstrap", mapOf("added" to false))
            return@runBlocking
        }

        log.info("Backup-root mangler – legger til...")

        val addResult = cli.run("add", backupRoot)

        if (addResult.exitCode == 0) {
            log.info("Backup-root ble lagt til OK.")
            sse.sendEnvelope("bootstrap", mapOf("added" to true))
        } else {
            log.error("Klarte ikke å legge til backup-root: ${addResult.output}")
            sse.sendEnvelope("bootstrap", mapOf("error" to addResult.output))
        }
    }
}
