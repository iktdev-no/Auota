package no.iktdev.auota.backup.parsing

object JottaIgnoreParser {
    fun parse(raw: String): Map<String, Set<String>> {
        val result = mutableMapOf<String, MutableSet<String>>()
        var currentBackup: String? = null

        raw.lineSequence().forEach { line ->
            when {
                line.startsWith("Listing patterns for backup:") -> {
                    currentBackup = line.removePrefix("Listing patterns for backup:").trim()
                    currentBackup?.let { result.putIfAbsent(it, mutableSetOf()) }
                }
                line.trim().startsWith(">") -> {
                    val pattern = line.trim().removePrefix(">").trim()
                    currentBackup?.let { result.getOrPut(it) { mutableSetOf() }.add(pattern) }
                }
            }
        }
        return result
    }
}
