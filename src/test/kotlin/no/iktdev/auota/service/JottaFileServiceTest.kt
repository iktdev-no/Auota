package no.iktdev.auota.service

import kotlinx.coroutines.test.runTest
import no.iktdev.auota.cli.JottaCli
import no.iktdev.auota.cli.JottaCliClient
import no.iktdev.auota.config.MoshiConfig
import no.iktdev.auota.models.files.FileType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class JottaFileServiceTest {
    val moshi = MoshiConfig().moshi()
    val cli = mock(JottaCliClient::class.java)
    val service = JottaFileService(cli, moshi)

    @Test
    @DisplayName("Jotta cloud folder parsing shoud work")
    fun `parses folder-only JSON`() = runTest {
        `when`(cli.run("ls", "--json"))
            .thenReturn(JottaCli.RunResult.Success(rootFolders))

        val result = service.explore("")
        assertThat(result).isNotNull
        assertThat(result!!.Files).isNullOrEmpty()
        assertThat(result.Folders).isNotEmpty
        assertThat(result.Folders!!.any { it.type == FileType.Folder })
    }

    val rootFolders = """
        {
          "Folders": [
            {
              "Name": "Archive",
              "Path": "/archive/"
            },
            {
              "Name": "Backup"
            },
            {
              "Name": "Photos",
              "Path": "/photos/"
            },
            {
              "Name": "Sync",
              "Path": "/sync/"
            },
            {
              "Name": "Trash",
              "Path": "/trash/"
            }
          ]
        }

    """.trimIndent()
}
