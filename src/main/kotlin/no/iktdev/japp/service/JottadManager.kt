package no.iktdev.japp.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.japp.models.JottaDaemonState
import org.springframework.stereotype.Service
import java.io.File
import java.io.RandomAccessFile

@Service
class JottadManager {

    private val log = KotlinLogging.logger {}

    val state = MutableStateFlow(JottaDaemonState.NOT_STARTED)

    @Volatile
    private var pid: Long? = null

    private val logFile = File("/root/.jottad/jottabackup.log")
    private val startupRegex =
        Regex("jottad\\.startup\\s*=>\\s*(\\w+)", RegexOption.IGNORE_CASE)

    suspend fun start() {
        if (state.value == JottaDaemonState.RUNNING) {
            log.info("Jottad already running (pid=$pid)")
            return
        }

        state.value = JottaDaemonState.STARTING
        log.info("Starting jottad daemon...")

        try {
            // Start jottad as a proper daemon process
            val process = withContext(Dispatchers.IO) {
                val pb = ProcessBuilder("jottad")
                pb.environment().apply {
                    put("HOME", "/root")
                    put("USER", "root")
                    put("LANG", "C.UTF-8")
                    put("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin")
                }

                // Important: do NOT redirect stdout/stderr to pipes that close early
                pb.redirectErrorStream(true)

                // Start the process
                pb.start()
            }

            // Java 9+ gives us the real PID
            pid = process.pid()

            log.info("Jottad started with PID $pid, waiting for startup result...")

            val startupResult = waitForStartupValue()

            when (startupResult.lowercase()) {
                "ok" -> {
                    log.info("Jottad startup OK → daemon is READY")
                    delay(500) // Gi jottad litt tid til å bli helt klar
                    state.value = JottaDaemonState.RUNNING
                }
                else -> {
                    log.error("Jottad startup FAILED → value was '$startupResult'")
                    state.value = JottaDaemonState.FAILED
                }
            }

        } catch (e: Exception) {
            log.error("Failed to start jottad", e)
            state.value = JottaDaemonState.FAILED
        }
    }

    private suspend fun waitForStartupValue(): String {
        return withContext(Dispatchers.IO) {

            // 1. Vent til loggfilen finnes
            while (!logFile.exists()) {
                delay(100)
            }

            val reader = RandomAccessFile(logFile, "r")
            reader.seek(reader.length())

            var startup: String? = null

            // 2. Tail til vi finner jottad.startup => <value>
            while (startup == null) {
                val line = reader.readLine()
                if (line == null) {
                    delay(100)
                    continue
                }

                val decoded = line.toByteArray(Charsets.ISO_8859_1)
                    .toString(Charsets.UTF_8)

                val match = startupRegex.find(decoded)
                if (match != null) {
                    val value = match.groupValues[1]
                    log.info("Detected jottad.startup => $value")
                    startup = value
                }
            }

            startup
        }
    }





    fun getPid(): Long? = pid
}
