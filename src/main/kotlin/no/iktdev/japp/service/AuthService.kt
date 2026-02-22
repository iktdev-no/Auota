package no.iktdev.japp.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.models.AuthResponse
import no.iktdev.japp.models.AuthStep
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.BufferedWriter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class AuthService(
    private val cli: JottaCli
) {
    private val log = KotlinLogging.logger {}

    private val sessions = ConcurrentHashMap<String, PendingSession>()
    private val timeoutMs = 60_000L * 5

    data class PendingSession(
        val process: Process,
        val stdin: BufferedWriter,
        val stdout: BufferedReader,
        val created: Long = System.currentTimeMillis()
    )

    private fun cleanupExpired() {
        val now = System.currentTimeMillis()
        sessions.entries.removeIf { (_, session) ->
            if (now - session.created > timeoutMs) {
                log.warn { "Destroying expired session (timeout): pid=${session.process.pid()}" }
                session.process.destroy()
                true
            } else false
        }
    }

    // -----------------------------
    // STEP DETECTION
    // -----------------------------
    private fun detectStep(output: String): AuthStep {
        val t = output.lowercase()

        return when {
            // License prompt
            "accept license" in t && "(yes/no)" in t ->
                AuthStep.LICENSE

            // PAT prompt
            "personal login token" in t ||
                    "personal access token" in t ->
                AuthStep.PAT

            // CLI is working
            "logging in" in t && "please wait" in t ->
                AuthStep.WAIT

            // Device name prompt
            "device name" in t ->
                AuthStep.DEVICE_NAME

            // Successful login
            "logged in as" in t ->
                AuthStep.DONE

            // PAT errors
            "invalid token" in t ||
                    "could not login" in t ||
                    "server did not recognize the provided credentials" in t ||
                    "authentication failed" in t ->
                AuthStep.ERROR

            // Generic error
            "error" in t && "login" in t ->
                AuthStep.ERROR

            else ->
                AuthStep.UNKNOWN
        }
    }

    // -----------------------------
    // RAW STREAM READER
    // -----------------------------
    private suspend fun readUntilPrompt(sessionId: String, session: PendingSession): String {
        val output = StringBuilder()

        withContext(Dispatchers.IO) {
            val reader = session.stdout
            var lastRead = System.currentTimeMillis()

            while (true) {
                if (reader.ready()) {
                    val ch = reader.read()
                    if (ch == -1) break

                    val c = ch.toChar()
                    print("jotta-cli[$sessionId] OUT: $c")
                    output.append(c)
                    lastRead = System.currentTimeMillis()
                } else {
                    if (System.currentTimeMillis() - lastRead > 200) break
                    Thread.sleep(10)
                }
            }
        }

        val text = output.toString().trim()
        log.info { "jotta-cli[$sessionId] RAW OUTPUT → FRONTEND:\n$text" }
        return text
    }

    // -----------------------------
    // START INTERACTIVE COMMAND
    // -----------------------------
    private suspend fun startInteractiveCommand(vararg args: String): AuthResponse {
        cleanupExpired()

        val p = cli.startInteractive(*args)
        val sessionId = UUID.randomUUID().toString()

        val session = PendingSession(
            process = p.process,
            stdin = p.stdin,
            stdout = p.stdout
        )
        sessions[sessionId] = session

        log.info { "jotta-cli[$sessionId] START: args=${args.toList()}" }

        val text = readUntilPrompt(sessionId, session)
        val step = detectStep(text)

        return AuthResponse(
            success = step != AuthStep.ERROR,
            message = if (text.isNotEmpty()) text else "Ingen output fra jotta-cli",
            step = step,
            sessionId = sessionId
        )
    }

    // -----------------------------
    // SEND INPUT
    // -----------------------------
    private suspend fun answerInteractiveCommand(
        sessionId: String,
        input: String,
        completeMessage: String
    ): AuthResponse {
        cleanupExpired()

        val session = sessions[sessionId]
            ?: return AuthResponse(false, "Session expired or not found", AuthStep.ERROR, null)

        log.info { "jotta-cli[$sessionId] IN: $input" }

        withContext(Dispatchers.IO) {
            session.stdin.write(input)
            session.stdin.write("\n")
            session.stdin.flush()
        }

        val text = readUntilPrompt(sessionId, session)
        val step = detectStep(text)

        // DONE or ERROR → finalize
        if (step == AuthStep.DONE || step == AuthStep.ERROR) {
            withContext(Dispatchers.IO) { session.process.waitFor() }
            val exit = session.process.exitValue()
            log.info { "jotta-cli[$sessionId] EXIT: $exit" }
            sessions.remove(sessionId)

            val ok = (exit == 0 && step == AuthStep.DONE)
            return AuthResponse(
                success = ok,
                message = if (ok) completeMessage else "CLI exited with code $exit\n\n$text",
                step = if (ok) AuthStep.DONE else AuthStep.ERROR,
                sessionId = null
            )
        }

        return AuthResponse(
            success = true,
            message = if (text.isNotEmpty()) text else "Ingen videre output fra jotta-cli",
            step = step,
            sessionId = sessionId
        )
    }

    // -----------------------------
    // POLLING ENDPOINT
    // -----------------------------
    suspend fun poll(sessionId: String): AuthResponse {
        cleanupExpired()

        val session = sessions[sessionId]
            ?: return AuthResponse(false, "Session expired or not found", AuthStep.ERROR, null)

        val text = readUntilPrompt(sessionId, session)
        val step = detectStep(text)

        // If CLI is dead, finalize
        if (!session.process.isAlive) {
            val exit = session.process.exitValue()
            sessions.remove(sessionId)

            val ok = (exit == 0 && step == AuthStep.DONE)
            return AuthResponse(
                success = ok,
                message = if (ok) "Login complete" else "CLI exited with code $exit\n\n$text",
                step = if (ok) AuthStep.DONE else AuthStep.ERROR,
                sessionId = null
            )
        }

        // If CLI reports error
        if (step == AuthStep.ERROR) {
            sessions.remove(sessionId)
            return AuthResponse(
                success = false,
                message = text,
                step = AuthStep.ERROR,
                sessionId = null
            )
        }

        return AuthResponse(
            success = true,
            message = text,
            step = step,
            sessionId = sessionId
        )
    }

    // -----------------------------
    // PUBLIC API
    // -----------------------------
    suspend fun startLogin(): AuthResponse =
        startInteractiveCommand("login")

    suspend fun answerLogin(sessionId: String, input: String): AuthResponse =
        answerInteractiveCommand(sessionId, input, "Login complete")

    suspend fun startLogout(): AuthResponse =
        startInteractiveCommand("logout")

    suspend fun answerLogout(sessionId: String, input: String): AuthResponse =
        answerInteractiveCommand(sessionId, input, "Logout complete")
}
