package no.iktdev.auota.models

data class OperationRequest(
    val duration: String? = null,
    val backupPath: String? = null
)

data class OperationResponse(
    val success: Boolean,
    val message: String,
    val raw: String? = null
)
