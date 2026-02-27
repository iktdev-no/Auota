package no.iktdev.auota.cli

interface JottaCliClient {
    suspend fun run(vararg args: String): JottaCli.RunResult
    suspend fun stream(vararg args: String, onLine: (String) -> Unit): JottaCli.StreamResult
    fun startInteractive(vararg args: String): JottaCli.InteractiveProcess
}
