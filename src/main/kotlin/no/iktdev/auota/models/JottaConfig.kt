package no.iktdev.auota.models

data class JottaConfig(
    val backuppaused: Boolean,
    val checksumreadrate: String,
    val downloadrate: String,
    val ignorehiddenfiles: Boolean,
    val logscanignores: Boolean,
    val logtransfers: Boolean,
    val maxdownloads: Int,
    val maxuploads: Int,
    val proxy: String,
    val scaninterval: String,
    val syncpaused: Boolean,
    val timeformat: String,
    val uploadrate: String,
    val usesiunits: Boolean,
    val webhookstatusinterval: String
)
