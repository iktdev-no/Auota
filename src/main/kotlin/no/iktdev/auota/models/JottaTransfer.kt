package no.iktdev.auota.models

data class JottaTransfer(
    val Id: String,
    val Remote: String,
    val Local: String,
    val Total: TransferTotal,
    val Remaining: TransferRemaining?,
    val CompletedTimeMs: Long?,
    val StartedTimeMs: Long?,
    val SelectionCount: TransferSelectionCount?,
    val Errors: TransferErrors?
)

data class TransferTotal(
    val Files: Int?,
    val Bytes: Long?
)

data class TransferRemaining(
    val Files: Int? = null,
    val Bytes: Long? = null
)

data class TransferSelectionCount(
    val Files: Int? = null,
    val Bytes: Long? = null
)

data class TransferErrors(
    val message: String? = null
)
