package no.iktdev.auota.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class JottaTransfer(
    val Id: String,
    val Remote: String?,              // nullable
    val Local: String?,               // nullable
    val Total: TransferTotal?,        // nullable
    val Remaining: TransferRemaining?,
    val CompletedTimeMs: Long?,
    val StartedTimeMs: Long?,
    val SelectionCount: TransferSelectionCount?,
    val Errors: TransferErrors?,
    val Selection: String? = null,
    val CriticalError: TransferErrors?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransferTotal(
    val Files: Int?,
    val Bytes: Long?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransferRemaining(
    val Files: Int? = null,
    val Bytes: Long? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransferSelectionCount(
    val Files: Int? = null,
    val Bytes: Long? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransferErrors(
    val message: String? = null
)
