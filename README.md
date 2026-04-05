# AUOTA – Jottacloud Backup Orchestrator for Linux

AUOTA er et web-basert UI for jotta-cli som gjør det mulig å overvåke og styre Jottacloud-backup på Linux.

Jottacloud tilbyr ingen offisiell GUI for Linux. AUOTA gir deg derfor:

- oversikt over alle backup-mapper
- fremdrift (prosent, bytes, filer)
- feilindikatorer
- siste oppdatering og neste backup
- trygg håndtering av “exclude vs delete”


---

# ⚠️ Viktig: Jottacloud sin sync-modell

Jottacloud bruker en destruktiv synkroniseringsmodell:

Hvis en mappe fjernes fra backup → slettes den i skyen.

Dette skjer uten advarsel.

Dette kan føre til sletting i skyen:

- manglende mounts
- manglende config/state
- feil i sync-logikk
- feilaktige exclude-regler
- manuell fjerning av mapper

Konklusjon:

Fjern aldri en mappe fra backup med mindre du faktisk ønsker å slette den i skyen.


---

# 📦 Oppsett (påkrevd mounts)

For at jotta-cli og AUOTA skal fungere korrekt, må følgende mounts være konfigurert:

```YAML
/path/to/your/data:/upload:ro                    # Mappe som lastes opp
/path/to/a/folder/for/auota/data:/root/.jottad   # Jottacloud state (device ID, metadata)
/path/to/a/folder/for/auota/config:/config       # AUOTA config
/path/to/a/download/folder:/download             # Nedlastninger
```

---

# 🔥 Kritiske mounts

Disse er absolutt nødvendige:

```YAML
/path/to/a/folder/for/auota/data:/root/.jottad
/path/to/a/folder/for/auota/config:/config
```

Hvis disse mangler kan det føre til:

- regenerering av device-ID
- tap av backup-definisjoner
- feil i sync-state
- SLETTING AV DATA I SKYEN
- desync mellom AUOTA og jotta-cli


---

# 🛡️ Trygg måte å stoppe backup

I stedet for å fjerne en mappe fra backup, bør du bruke:

Deaktiver backup

Dette gjør:

- stopper synkronisering
- beholder data i skyen
- forhindrer sletting
- unngår destruktiv sync


Hvordan det fungerer:

AUOTA bruker jotta-cli ignores under panseret:

- det legges til en ignore-regel
- Jottacloud ignorerer endringer
- ingen filer slettes

Dette er den anbefalte måten å deaktivere backup på.


---

# 📤 Arkiv-upload via /data

Filer plassert i:

```YAML
/path/to/a/folder/for/data:/data
```
kan lastes opp til Arkiv i Jottacloud.


⚠️ Begrensninger:

- fungerer best for mindre filer
- store filer kan feile eller stoppe opp
- for store filer bør vanlig backup brukes


---

# 📊 Funksjoner

AUOTA gir:

- totalt antall filer
- total størrelse
- aktive opplastinger
- gjenstående data
- prosent ferdig
- progressbar
- feilindikatorer
- siste oppdatering
- neste backup

I tillegg:

- trygg deaktivering av backup
- beskyttelse mot destruktiv sync


---

# 🧹 Anbefalt bruk

Før større endringer:

- verifiser at alle mounts er riktig satt opp
- ikke start uten data og config
- ikke fjern mapper direkte fra backup
- bruk deaktivering hvis du er usikker
- sjekk UI for mismatch før sync


---

# 🧠 Hvorfor AUOTA finnes

Jottacloud på Linux er begrenset til jotta-cli.

Dette gjør det vanskelig å:

- få oversikt over status og fremdrift
- forstå hva som skjer under synkronisering
- håndtere store backup-sett
- unngå utilsiktet sletting


I tillegg:

- fjerning av mapper → sletting i skyen
- manglende state → kan tolkes som sletting
- feil konfigurasjon → potensielt datatap


---

# 🧩 Oppsummert

AUOTA gjør Jottacloud på Linux:

- mer oversiktlig
- enklere å bruke
- tryggere 

___

# Docker compose eksempel:
```yaml
services:
  auota:
    container_name: auota
    image: bskjon/auota
    restart: unless-stopped
    ports:
      - 4081:8080
    volumes:
      - /media/remote:/upload:ro
      - ./auota/data:/root/.jottad
      - ./auota/config:/config
```

⚠️ Ikke endre eller slett /root/.jottad eller mappen den er montert til etter at containeren er startet.
Dette kan føre til at Jottacloud tolker det som at alle filer er fjernet.