package no.iktdev.japp.service

import no.iktdev.japp.cli.JottaCli
import no.iktdev.japp.encrypt.EncryptionManager
import no.iktdev.japp.service.status.JottaStatusService
import no.iktdev.japp.sse.SseHub
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class JottaStatusServiceTest {

    private val cli = mock<JottaCli>()
    private val sse = mock<SseHub>()
    private val encryptionManager = mock<EncryptionManager>()
    private val jottadManager = mock<JottadManager>()

    val service = JottaStatusService(
        cli = cli,
        sse = sse,
        encryptionManager = encryptionManager,
        jottadManager = jottadManager
    )

    @Test
    fun `should parse status json correctly`() {
        val result = service.getJsonStatus(statusJson)

        assertNotNull(result)
    }


    val statusJson = """
        {
          "User": {
            "Email": "redacted@jotta.lan",
            "Fullname": "Potato master",
            "Avatar": {
              "Initials": "PM",
              "Background": {
                "r": 228,
                "g": 23,
                "b": 92
              }
            },
            "Brand": "Telia Sky",
            "Hostname": "dc11c787b858",
            "AccountInfo": {
              "Capacity": -1,
              "Usage": 934265012,
              "Subscription": 3,
              "SubscriptionNameLocalized": "Ubegrenset lagring",
              "ProductNameLocalized": "Ubegrenset lagring"
            },
            "device": {
              "Name": "Kaze - Docker",
              "Type": 12
            }
          },
          "Sync": {
            "Count": {},
            "RemoteCount": {}
          },
          "State": {
            "RestoreWorking": true,
            "Uploading": {},
            "Downloading": {},
            "LastTokenRefresh": 1771803625
          },
          "Backup": {
            "State": {
              "Enabled": {
                "deviceName": "Kaze - Docker",
                "Backups": [
                  {
                    "Name": "data",
                    "Path": "/data",
                    "Count": {},
                    "Uploading": {},
                    "Errors": {},
                    "DeviceID": "9e551c2d-6cf5-4196-8548-5472ef9a6f18",
                    "ErrorFilesCount": {},
                    "ErrorFoldersCount": 1,
                    "History": [
                      {
                        "Path": "/data",
                        "Upload": {
                          "Started": {},
                          "Completed": {}
                        },
                        "Started": 1771804334,
                        "Ended": 1771804334,
                        "Finished": true,
                        "Total": {}
                      }
                    ],
                    "LastUpdateMS": 1771804339799,
                    "LastScanStartedMS": 1771804339799,
                    "NextBackupMS": 1771807939799
                  }
                ]
              }
            }
          }
        }
    """.trimIndent()
}