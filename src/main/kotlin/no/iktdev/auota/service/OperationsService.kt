package no.iktdev.auota.service

import no.iktdev.auota.cli.JottaCli
import no.iktdev.auota.models.OperationResponse
import org.springframework.stereotype.Service

@Service
class OperationsService(
    private val cli: JottaCli
) {

    suspend fun pause(duration: String? = null, backupPath: String? = null): OperationResponse {
        val args = mutableListOf("pause")

        if (duration != null) {
            args.add(duration)
        }

        if (backupPath != null) {
            args.add("--backup")
            args.add(backupPath)
        }

        return when (val result = cli.run(*args.toTypedArray())) {
            is JottaCli.RunResult.Success ->
                OperationResponse(true, "Paused successfully", result.output)

            is JottaCli.RunResult.Error ->
                OperationResponse(false, "Pause failed: ${result.output}", result.output)
        }
    }

    suspend fun resume(): OperationResponse {
        return when (val result = cli.run("resume")) {
            is JottaCli.RunResult.Success ->
                OperationResponse(true, "Resumed successfully", result.output)

            is JottaCli.RunResult.Error ->
                OperationResponse(false, "Resume failed: ${result.output}", result.output)
        }
    }
}
