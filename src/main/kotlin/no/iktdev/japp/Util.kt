package no.iktdev.japp

import com.fasterxml.jackson.databind.ObjectMapper
import no.iktdev.japp.models.SseEnvelope
import java.security.MessageDigest


fun sha256Hex(data: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(data)
    return digest.joinToString("") { "%02x".format(it) }
}