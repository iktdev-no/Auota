package no.iktdev.japp.backup.sync

import mu.KotlinLogging
import no.iktdev.japp.cli.JottaCli

class RootSyncer(
    private val cli: JottaCli
) {
    private val log = KotlinLogging.logger {}

    suspend fun sync(expectedRoots: Set<String>, actualRoots: Set<String>) {
        val toAdd = expectedRoots - actualRoots
        val toRemove = actualRoots - expectedRoots

        toAdd.forEach { root ->
            log.info { "Adding missing root $root" }
            cli.run("add", root)
        }

        toRemove.forEach { root ->
            log.warn { "Removing root $root (destructive!)" }
            cli.run("rem", root)
        }
    }
}