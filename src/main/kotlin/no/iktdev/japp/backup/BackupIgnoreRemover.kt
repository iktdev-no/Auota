package no.iktdev.japp.backup

import mu.KotlinLogging
import no.iktdev.japp.cli.JottaCli

class BackupIgnoreRemover(private val cli: JottaCli) {
    private val log = KotlinLogging.logger {}

    /**
     * Forsøker flere varianter for å fjerne et ignore pattern.
     * existsCheck må sjekke om pattern fortsatt finnes.
     */
    suspend fun forceRemove(
        root: String,
        pattern: String,
        existsCheck: suspend (String, String) -> Boolean
    ) {
        val patterns = listOf(
            pattern,
            "\"$pattern\"",
            "'$pattern'"
        )

        val backups = listOf(
            root,
            "$root/"
        )

        val attempts = mutableListOf<Pair<String, String>>()
        for (p in patterns) {
            for (b in backups) {
                attempts += p to b
            }
        }

        for ((pat, backup) in attempts) {

            val cmd = listOf("ignores", "rem", "--pattern", pat, "--backup", backup)
            log.info { "Trying ignore removal: ${cmd.joinToString(" ")}" }

            val result = cli.run(*cmd.toTypedArray())

            if (result.exitCode != 0) {
                log.error {
                    "Ignore removal attempt failed (exit=${result.exitCode}): " +
                            "cmd='${cmd.joinToString(" ")}' output='${result.output.trim()}'"
                }
            } else {
                log.info {
                    "Ignore removal command executed (exit=0): " +
                            "cmd='${cmd.joinToString(" ")}' output='${result.output.trim()}'"
                }
            }

            // sjekk om pattern fortsatt finnes
            val stillThere = existsCheck(root, pattern)
            if (!stillThere) {
                log.info {
                    "Successfully removed ignore '$pattern' from $root using pattern='$pat' backup='$backup'"
                }
                return
            } else {
                log.error {
                    "Pattern '$pattern' still present after attempt: pattern='$pat' backup='$backup'"
                }
            }
        }

        log.error { "Could not remove ignore '$pattern' from $root after all attempts; marking as unremovable" }
    }
}
