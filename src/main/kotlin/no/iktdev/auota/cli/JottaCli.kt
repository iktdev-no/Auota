package no.iktdev.auota.cli

import com.github.pgreze.process.ProcessResult
import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Component
class JottaCli {
    private val executable = "jotta-cli"

    suspend fun run(
        vararg args: String,
    ): RunResult {
        val output = mutableListOf<String>()
        val result = runCommand(args.toList(), {
            output.add(it)
        }, Redirect.CAPTURE)
        return if (result.resultCode == 0) {
            RunResult.Success(result.output.joinToString("\n"))
        } else {
            RunResult.Error(result.output.joinToString("\n"), result.resultCode)
        }
    }

    suspend fun stream(
        vararg args: String,
        onLine: (String) -> Unit
    ): StreamResult {
        val result = runCommand(args.toList(), onLine, Redirect.CAPTURE)
        return if (result.resultCode == 0) {
            StreamResult.Success()
        } else {
            StreamResult.Error(result.resultCode)
        }
    }

    internal suspend fun runCommand(arguments: List<String>, output: (String) -> Unit, mode: Redirect = Redirect.CAPTURE): ProcessResult {
        return process(executable, *arguments.toTypedArray(),
            stdout = mode,
            stderr = mode,
            consumer = {
                output(it)
            },
            destroyForcibly = true
        )
    }

    /**
     * Starter en interaktiv prosess og returnerer stdin/stdout + prosess.
     * Dette brukes for login/logout.
     */
    fun startInteractive(vararg args: String): InteractiveProcess {
        val process = ProcessBuilder(executable, *args)
            .redirectErrorStream(true)
            .start()
        val stdin = BufferedWriter(OutputStreamWriter(process.outputStream))
        val stdout = BufferedReader(InputStreamReader(process.inputStream))
        return InteractiveProcess(process, stdin, stdout)
    }

    data class InteractiveProcess(
        val process: Process,
        val stdin: BufferedWriter,
        val stdout: BufferedReader
    )

    sealed class RunResult(
        val output: String,
        val exitCode: Int
    ) {
        class Success(output: String) : RunResult(output, 0)
        class Error(output: String, exitCode: Int) : RunResult(output, exitCode)
    }
    sealed class StreamResult(
        val exitCode: Int,
    ) {
        class Success() : StreamResult(exitCode = 0)
        class Error(exitCode: Int) : StreamResult(exitCode = exitCode)
    }
}