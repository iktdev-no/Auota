package no.iktdev.japp.models.backup

data class BackupItem(
    val path: String,
    val excludePaths: List<String>,
)