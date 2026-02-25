package no.iktdev.japp.jotta

import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.models.JottaVersionInfo
import org.springframework.stereotype.Component

@Component
class JottaVersion(
    private val cli: JottaCli
) {
    suspend fun getVersion(): JottaVersionInfo {
        val result = cli.run("version")
        val parsed = parse(result.output)
        return parsed
    }


    private fun parse(output: String): JottaVersionInfo {
        val lines = output
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("---") }

        val map = mutableMapOf<String, String>()

        for (line in lines) {
            val idx = line.indexOf(":")
            if (idx == -1) continue

            val key = line.substring(0, idx).trim()
            val value = line.substring(idx + 1).trim()
            map[key] = value
        }

        return JottaVersionInfo(
            jottadExecutable = map["jottad executable"] ?: "",
            jottadAppdata = map["jottad appdata"] ?: "",
            jottadLogfile = map["jottad logfile"] ?: "",
            jottadVersion = map["jottad version"] ?: "",
            remoteVersion = map["remote version"] ?: "",
            jottaCliVersion = map["jotta-cli version"] ?: "",
            releaseNotes = map["release notes"] ?: ""
        )
    }


}