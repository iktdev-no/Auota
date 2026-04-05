package no.iktdev.auota.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.auota.models.JottaDaemonState
import org.springframework.stereotype.Service
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files

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

    suspend fun restart() {
        log.warn("Restarting jottad daemon…")

        try {
            stopIfRunning()
        } catch (e: Exception) {
            log.error("Failed to stop jottad before restart", e)
        }

        start()
    }

    private fun stopIfRunning() {
        val currentPid = pid ?: return

        val handle = ProcessHandle.of(currentPid)
        if (handle.isPresent && handle.get().isAlive) {
            log.info("Stopping jottad (pid=$currentPid)")
            handle.get().destroy()
            Thread.sleep(300)
        }

        pid = null
        state.value = JottaDaemonState.NOT_STARTED
    }


    private suspend fun waitForStartupValue(): String = withContext(Dispatchers.IO) {

        val path = logFile.toPath()

        // 1. Wait until file exists
        while (!Files.exists(path)) {
            delay(100)
        }

        // 2. Open a UTF-8 reader
        Files.newBufferedReader(path, Charsets.UTF_8).use { reader ->

            var startup: String? = null
            var line: String?

            // 3. First pass: read entire existing file
            while (reader.readLine().also { line = it } != null) {
                val match = startupRegex.find(line!!)
                if (match != null) {
                    startup = match.groupValues[1]
                    return@withContext startup
                }
            }

            // 4. Tail mode: watch for new lines
            val startTime = System.currentTimeMillis()
            val timeoutMs = 15_000L // 15 seconds

            while (startup == null) {

                // Timeout safety
                if (System.currentTimeMillis() - startTime > timeoutMs) {
                    return@withContext "timeout"
                }

                line = reader.readLine()

                if (line == null) {
                    delay(100)
                    continue
                }

                val match = startupRegex.find(line)
                if (match != null) {
                    startup = match.groupValues[1]
                    return@withContext startup
                }
            }

            startup
        }
    }


    fun getPid(): Long? = pid
}
