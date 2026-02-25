package no.iktdev.auota.backup.parsing

object JottaIgnoreNormalizer {

    /**
     * Returnerer normalisert pattern relativt til root, eller null hvis pattern er en ghost/ugyldig.
     */
    fun normalize(root: String, pattern: String): String? {
        var p = pattern.trim()
        if (p.startsWith("/")) p = p.removePrefix("/")
        val rootClean = root.trim().removePrefix("/")

        // Hvis pattern starter med root (ghost), fjern root prefix
        if (p.startsWith(rootClean)) {
            p = p.removePrefix(rootClean).removePrefix("/")
        }

        // Fjern trailing slash
        if (p.endsWith("/")) p = p.removeSuffix("/")

        // Hvis tomt eller global wildcard -> ghost/ugyldig
        if (p.isBlank() || p == "**") return null

        return p
    }

    fun isGhost(root: String, pattern: String): Boolean = normalize(root, pattern) == null
}
