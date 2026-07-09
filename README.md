# MicroGreensApp

App Android nativa (Kotlin + Jetpack Compose) per tenere traccia di tutto il
processo di coltivazione dei microgreens, organizzato per vassoi: template di
fasi personalizzabili, calendario unico degli eventi, notifiche, foto, dati
meteo/alba-tramonto e statistiche. Uso personale, dati 100% locali (Room), nessun account.

Il piano di implementazione completo è in `/root/.claude/plans/logical-discovering-pinwheel.md` (sessione di sviluppo) ed è riassunto nelle 6 fasi qui sotto.

## Stato di avanzamento

- [x] **Fase 1** — Scaffold progetto (Gradle/Kotlin/Compose/Hilt/Room) e CRUD
      completo dei template di varietà (metadati + step riordinabili), con
      il template "Girasole" precaricato al primo avvio. Build verificata
      con successo su GitHub Actions.
- [x] **Fase 2** — Vassoi con snapshot del piano dal template scelto,
      timeline unificata di step pianificati + eventi liberi per vassoio
      (segna fatto/salta/modifica senza toccare il template), calendario
      mensile con filtro per vassoio, bottom nav (Calendario/Vassoi/
      Statistiche/Impostazioni). Build verificata con successo su GitHub
      Actions.
- [x] **Fase 3** — Promemoria locali con WorkManager derivati dagli step
      pianificati (orari di sciacquo/irrigazione/raccolta ecc.), ricalcolati
      automaticamente ad ogni modifica del piano di un vassoio, con tap
      sulla notifica che apre il vassoio corrispondente. Richiesta permesso
      notifiche dalle Impostazioni. Build verificata con successo su
      GitHub Actions.
- [x] **Fase 4** — Meteo Open-Meteo: ricerca e impostazione posizione dalle
      Impostazioni (cambiabile in qualsiasi momento), temperatura/umidità/
      alba-tramonto del giorno pre-compilate nel form evento ma sempre
      sovrascrivibili, con cache giornaliera locale. Chiamate dirette con
      OkHttp + kotlinx.serialization. Build verificata con successo su
      GitHub Actions.
- [ ] **Fase 5** (in verifica) — Foto: scatto con fotocamera di sistema o
      selezione dalla galleria (Photo Picker, nessun permesso storage
      richiesto), storage privato dell'app (`filesDir/photos`, eliminato
      alla disinstallazione), galleria orizzontale sul vassoio e sul singolo
      evento con visualizzatore a schermo intero ed eliminazione.
- [ ] Fase 6 — Statistiche

## Build

Il progetto richiede l'Android SDK (compileSdk 35, minSdk 26). Per compilare:

```bash
./gradlew assembleDebug
```

Un workflow GitHub Actions (`.github/workflows/android-build.yml`) compila
automaticamente l'APK debug ad ogni push e lo carica come artifact scaricabile
dalla scheda "Actions" del repository.

## Struttura

```
core/database      Room: entity, dao, converter, seed
core/di            Moduli Hilt
core/network       Client OkHttp Open-Meteo (geocoding + forecast) e DTO
core/notifications Canale, scheduler WorkManager e worker dei promemoria
core/repository    Repository che orchestrano DAO/rete/DataStore per le ViewModel
feature/template   CRUD template di varietà e step
feature/tray       Vassoi: lista, creazione, dettaglio/timeline/galleria foto
feature/event      Form aggiungi/modifica evento (meteo pre-compilato, foto)
feature/calendar   Vista mensile con filtro per vassoio
feature/stats      Placeholder (Fase 6)
feature/settings   Permesso notifiche, ricerca/impostazione posizione meteo
navigation         Grafo di navigazione Compose (rotte type-safe) + bottom nav
ui, ui/theme       Componenti condivisi (date picker, galleria foto, colori/etichette) e tema Material 3
```
