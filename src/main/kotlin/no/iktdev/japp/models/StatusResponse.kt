package no.iktdev.japp.models

data class StatusResponse(
    val success: Boolean,
    val raw: String,
    val parsed: JottaStatus? = null,
    val message: String? = null
)


data class JottaStatus(
    val User: UserInfo?,
    val Sync: SyncInfo?,
    val State: GlobalState?,
    val Backup: BackupInfo?
)

data class UserInfo(
    val Email: String?,
    val Fullname: String?,
    val Avatar: AvatarInfo?,
    val Brand: String?,
    val Hostname: String?,
    val AccountInfo: AccountInfo?,
    val device: DeviceInfo?
)

data class AvatarInfo(
    val Initials: String?,
    val Background: RgbColor?
)

data class RgbColor(
    val r: Int,
    val g: Int,
    val b: Int
)

data class AccountInfo(
    val Capacity: Long,
    val Usage: Long,
    val Subscription: Int
)

data class DeviceInfo(
    val Name: String?,
    val Type: Int?
)

data class SyncInfo(
    val Count: Map<String, Any>?,
    val RemoteCount: Map<String, Any>?
)

data class GlobalState(
    val RestoreWorking: Boolean?,
    val Uploading: Map<String, Any>?,
    val Downloading: Map<String, Any>?,
    val LastTokenRefresh: Long?
)

data class BackupInfo(
    val State: BackupState?
)

data class BackupState(
    val Enabled: EnabledBackup?
)

data class EnabledBackup(
    val deviceName: String?,
    val Backups: List<BackupFolder>?
)

data class BackupFolder(
    val Name: String?,
    val Path: String?,
    val Count: Map<String, Any>?,
    val Uploading: Map<String, Any>?,
    val Errors: Map<String, Any>?,
    val DeviceID: String?,
    val ErrorFilesCount: Map<String, Any>?,
    val ErrorFoldersCount: Int?,
    val History: List<BackupHistory>?,
    val LastUpdateMS: Long?,
    val LastScanStartedMS: Long?,
    val NextBackupMS: Long?
)

data class BackupHistory(
    val Path: String?,
    val Upload: UploadHistory?,
    val Started: Long?,
    val Ended: Long?,
    val Finished: Boolean?,
    val Total: Map<String, Any>?
)

data class UploadHistory(
    val Started: Map<String, Any>?,
    val Completed: Map<String, Any>?
)
