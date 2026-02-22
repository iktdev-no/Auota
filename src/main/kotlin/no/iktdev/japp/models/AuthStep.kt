package no.iktdev.japp.models

enum class AuthStep {
    LICENSE,   // accept license (yes/no)
    PAT,       // personal access token
    WAIT,      // vent på at CLI skal gjøre noe, ingen input,
    DEVICE_NAME, // CLI spør etter device name
    DONE,      // ferdig, exit code 0
    ERROR,     // ferdig, exit code != 0 eller feilmelding
    UNKNOWN    // alt annet – vis tekst + fritekst-input
}

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val step: AuthStep,
    val sessionId: String? = null
)
