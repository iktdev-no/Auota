package no.iktdev.japp.models

enum class EncryptionState {
    NOT_INITIALIZED,   // Ingen backend, ingen config, ingen mount
    INITIALIZING,      // Init-sekvens kjører
    RESTORING,         // Systemet forsøker å rekonstruere eller validere backend
    READY,             // Alt OK
    FAILED,            // gocryptfs feilet (init/mount/verify)
    REJECTED,          // Vi avviser backend pga mismatch, manglende metadata, osv.
    NOT_ENABLED,       // Encryption er avskrudd i config
    TEARDOWN,           // Vi demonterer og rydder ned
    MANUAL_OVERRIDE,     // Manuell overstyring er aktivert (f.eks. slå av automatisk oppførsel)
}
