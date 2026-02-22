package no.iktdev.japp.service

import no.iktdev.japp.cli.JottaCli
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class BackupService(
    private val cli: JottaCli,
    @Value("\${backup.root}") private val backupRoot: String
) {

    suspend fun add(): JottaCli.RunResult {
        return cli.run("add", backupRoot)
    }

    suspend fun status(): JottaCli.RunResult {
        return cli.run("status")
    }

    suspend fun scan(): JottaCli.RunResult {
        return cli.run("scan")
    }

    suspend fun pause(duration: String? = null): JottaCli.RunResult {
        return if (duration != null)
            cli.run("pause", duration)
        else
            cli.run("pause")
    }

    suspend fun resume(): JottaCli.RunResult {
        return cli.run("resume")
    }

    suspend fun remove(): JottaCli.RunResult {
        return cli.run("rem", backupRoot)
    }
}
