package no.iktdev.ts

// En regel for å override TS-literal per klasse
data class TSRulesEntry(
    val targetClassName: String,   // <-- bruk String i stedet for KClass
    val tsLiteralOverride: String? = null
)

// Holder alle regler og tilbyr lookup
class Rules(private val entries: List<TSRulesEntry> = emptyList()) {

    // Finn en regel for en klasse
    fun getOverride(cls: Class<*>): String? {
        return entries.firstOrNull { it.targetClassName == cls.name }?.tsLiteralOverride
    }

    companion object {
        // Eksempel: lage noen default regler med fullt kvalifisert klassename
        val default = Rules(
            listOf(
                TSRulesEntry(targetClassName = "no.iktdev.auota.models.jottaFs.JottaFile", tsLiteralOverride = "File"),
                TSRulesEntry(targetClassName = "no.iktdev.auota.models.jottaFs.JottaFolder", tsLiteralOverride = "Folder")
            )
        )
    }
}