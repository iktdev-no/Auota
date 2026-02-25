package no.iktdev.japp.models

data class JottaSummary(
    val success: Boolean,
    val raw: String,
    val parsed: JottaStatus? = null,
    val message: String? = null
)

data class JottaStatus(
    val User: UserInfo? = null,
    val Sync: SyncInfo? = null,
    val State: GlobalState? = null,
    val Backup: BackupInfo? = null
)

data class UserInfo(
    val Email: String? = null,
    val Fullname: String? = null,
    val Avatar: AvatarInfo? = null,
    val Brand: String? = null,
    val Hostname: String? = null,
    val AccountInfo: AccountInfo? = null,
    val device: DeviceInfo? = null
)

data class AvatarInfo(
    val Initials: String? = null,
    val Background: RgbColor? = null
)

data class RgbColor(
    val r: Int? = null,
    val g: Int? = null,
    val b: Int? = null
)

data class AccountInfo(
    val Capacity: Long? = null,
    val Usage: Long? = null,
    val Subscription: Int? = null,
    val SubscriptionNameLocalized: String? = null,
    val ProductNameLocalized: String? = null
)

data class DeviceInfo(
    val Name: String? = null,
    val Type: Int? = null
)

data class SyncInfo(
    val Count: Map<String, Any>? = null,
    val RemoteCount: Map<String, Any>? = null
)

data class GlobalState(
    val RestoreWorking: Boolean? = null,
    val Uploading: Map<String, Any>? = null,
    val Downloading: Map<String, Any>? = null,
    val LastTokenRefresh: Long? = null
)

data class BackupInfo(
    val State: BackupState? = null
)

data class BackupState(
    val Enabled: EnabledBackup? = null
)

data class EnabledBackup(
    val deviceName: String? = null,
    val Backups: List<BackupFolder>? = null
)

data class BackupFolder(
    val Name: String? = null,
    val Path: String? = null,
    val Count: Map<String, Any>? = null,
    val Uploading: Map<String, Any>? = null,
    val Errors: Map<String, Any>? = null,
    val DeviceID: String? = null,
    val ErrorFilesCount: Map<String, Any>? = null,
    val ErrorFoldersCount: Int? = null,
    val History: List<BackupHistory>? = null,
    val LastUpdateMS: Long? = null,
    val LastScanStartedMS: Long? = null,
    val NextBackupMS: Long? = null
)

data class BackupHistory(
    val Path: String? = null,
    val Upload: UploadHistory? = null,
    val Started: Long? = null,
    val Ended: Long? = null,
    val Finished: Boolean? = null,
    val Total: Map<String, Any>? = null
)

data class UploadHistory(
    val Started: Map<String, Any>? = null,
    val Completed: Map<String, Any>? = null
)
