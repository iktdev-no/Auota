package no.iktdev.japp.encrypt.operations

import no.iktdev.japp.cli.RunCli
import no.iktdev.japp.encrypt.backend.BackendPaths

class TeardownOperation(
    private val runCli: RunCli,
    private val paths: BackendPaths
) {
    suspend fun unmount(): Boolean {
        val result = runCli.runCommand("fusermount", listOf("-u", paths.mount.toString()))
        return result.resultCode == 0
    }
}
