package no.iktdev.auota.models.backup

data class BackupItem(
    val path: String,
    val excludePaths: List<String>,
)