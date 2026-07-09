# MicroGreensApp

App Android nativa (Kotlin + Jetpack Compose) per tenere traccia di tutto il
processo di coltivazione dei microgreens, organizzato per vassoi: template di
fasi personalizzabili, calendario unico degli eventi, notifiche, foto, dati
meteo/alba-tramonto e statistiche. Uso personale, dati 100% locali (Room), nessun account.

Il piano di implementazione completo è in `/root/.claude/plans/logical-discovering-pinwheel.md` (sessione di sviluppo) ed è riassunto nelle 6 fasi qui sotto.

## Stato di avanzamento

- [x] **Fase 1** — Scaffold progetto (Gradle/Kotlin/Compose/Hilt/Room) e CRUD
      completo dei template di varietà (metadati + step riordinabili), con
      il template "Girasole" precaricato al primo avvio.
- [ ] Fase 2 — Vassoi, eventi, calendario
- [ ] Fase 3 — Notifiche
- [ ] Fase 4 — Meteo (Open-Meteo)
- [ ] Fase 5 — Foto
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
core/database   Room: entity, dao, converter, seed
core/di         Moduli Hilt
core/repository Repository che orchestrano i DAO per le ViewModel
feature/template CRUD template di varietà e step
navigation      Grafo di navigazione Compose (rotte type-safe)
ui/theme        Tema Material 3
```
