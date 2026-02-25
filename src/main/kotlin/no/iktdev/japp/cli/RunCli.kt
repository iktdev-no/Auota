package no.iktdev.japp.cli

import com.github.pgreze.process.ProcessResult
import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import org.springframework.stereotype.Component

@Component
class RunCli {

    suspend fun runCommand(executable: String, arguments: List<String>, output: (String) -> Unit, mode: Redirect = Redirect.CAPTURE): ProcessResult {
        return process(executable, *arguments.toTypedArray(),
            stdout = mode,
            stderr = mode,
            consumer = {
                output(it)
            },
            destroyForcibly = true
        )
    }
    suspend fun runCommand(executable: String, arguments: List<String>): ProcessResult {
        return process(executable, *arguments.toTypedArray(),
            stdout =  Redirect.CAPTURE,
            stderr =  Redirect.CAPTURE,
            destroyForcibly = true
        )
    }
}