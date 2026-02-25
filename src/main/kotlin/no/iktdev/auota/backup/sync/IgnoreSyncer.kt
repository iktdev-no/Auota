package no.iktdev.auota.backup.sync

import no.iktdev.auota.cli.JottaCli
import mu.KotlinLogging
import no.iktdev.auota.backup.BackupIgnoreRemover
import no.iktdev.auota.backup.parsing.JottaIgnoreNormalizer
import no.iktdev.auota.backup.parsing.JottaIgnoreParser
import java.nio.file.Path

class IgnoreSyncer(
    private val cli: JottaCli,
    private val remover: BackupIgnoreRemover
) {
    private val log = KotlinLogging.logger {}

    suspend fun syncIgnoresForRoot(root: String, expectedExcludePaths: Set<String>, rawIgnoresOutput: String) {
        val parser = JottaIgnoreParser
        val normalizer = JottaIgnoreNormalizer

        val ignoresByBackup = parser.parse(rawIgnoresOutput)
        val actualRawPatterns = ignoresByBackup[root] ?: emptySet()

        // expectedPatterns: relative patterns like "sub/folder/**"
        val expectedPatterns = expectedExcludePaths.map { exclude ->
            val rootPath = Path.of(root)
            val excludePath = Path.of(exclude)
            val relative = rootPath.relativize(excludePath).toString().replace("\\", "/")
            "$relative/**"
        }.toSet()

        val actualPatternsNormalized = actualRawPatterns.mapNotNull { normalizer.normalize(root, it) }.toSet()

        val toAdd = expectedPatterns - actualPatternsNormalized
        val toRemove = actualPatternsNormalized.filter { actual ->
            expectedPatterns.none { expected -> patternsOverlap(expected, actual) }
        }

        toAdd.forEach { pattern ->
            log.info { "Adding ignore $pattern to $root" }
            cli.run("ignores", "add", "--pattern", pattern, "--backup", root)
        }

        toRemove.forEach { pattern ->
            log.info { "Removing ignore $pattern from $root" }
            remover.forceRemove(root, pattern) { r, p ->
                val updated = JottaIgnoreParser.parse(cli.run("ignores", "list").output)
                // sjekk mot raw patterns (ikke-normalisert) for å være konservativ
                updated[r]?.any { raw -> JottaIgnoreNormalizer.normalize(r, raw) == p } == true
            }
        }
    }

    private fun patternsOverlap(a: String, b: String): Boolean {
        val aBase = a.removeSuffix("/**")
        val bBase = b.removeSuffix("/**")
        return aBase.startsWith(bBase) || bBase.startsWith(aBase)
    }
}
