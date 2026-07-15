# MicroGreensApp

App Android nativa (Kotlin + Jetpack Compose) per tracciare tutto il ciclo
di coltivazione dei microgreens, organizzato per vassoi. Uso personale,
dati 100% locali (Room), nessun account, nessun cloud.

## Funzionalità

### Varietà e piani di coltivazione

Ogni varietà è un template con una sequenza di step (ammollo, prevenzione
muffa, trasferimento nel vassoio, crescita, raccolta, conservazione),
ciascuno con offset in giorni dalla semina, orari di promemoria e
istruzioni. Gli step sono riordinabili via drag-and-drop. Due varietà sono
precaricate al primo avvio (Girasole, Piselli) con piani realistici,
completamente modificabili.

Modificare un template **non tocca** i vassoi già creati da quel
template: alla creazione il piano viene copiato (snapshot) sul vassoio, che
da quel momento è indipendente.

### Vassoi

- Creazione da un template, con nome, data di semina, quantità semi e
  substrato.
- Tab "In corso" (dashboard con giorni dalla semina, barra di progresso,
  stima del giorno di raccolto) e "Raccolti" (resa totale e durata
  effettiva per ciclo), navigabili anche con swipe.
- Timeline unica per vassoio, che unisce step pianificati ed eventi
  registrati liberamente, raggruppata per data (Oggi/Ieri/Domani/data) e
  con badge di stato (Fatto/Saltato) e di aderenza al piano.
- Registrare un raccolto (da uno step o da un evento libero) porta il
  vassoio automaticamente allo stato "Raccolto".
- Se esiste già almeno un ciclo raccolto della stessa varietà, il
  dettaglio del vassoio in corso mostra una previsione del raccolto basata
  sui semi usati × la resa media storica per grammo di seme.

### Calendario

Prima cosa che si vede aprendo l'app: una sezione "Oggi" con tutte le
azioni pianificate per la giornata su tutti i vassoi, filtrabile per
vassoio, con spunta diretta per segnare fatto uno step (quantità richiesta
solo per la raccolta) senza dover entrare nel dettaglio del vassoio.
Sotto, dietro un toggle "Mese", la vista mensile classica con un pallino
colorato per varietà su ogni giorno con eventi; tap su un giorno diverso
da oggi per vederne gli eventi.

### Notifiche

Promemoria locali (WorkManager) derivati dagli step pianificati,
ricalcolati automaticamente ad ogni modifica del piano di un vassoio. Un
interruttore in Opzioni li attiva/disattiva in qualsiasi momento,
indipendentemente dal permesso di sistema.

### Meteo

Temperatura e umidità del giorno, recuperate da Open-Meteo in base alla
posizione impostata in Opzioni, pre-compilate (ma sempre modificabili) su
ogni evento registrato — sia segnando fatto uno step pianificato, sia
aggiungendo un evento libero. In Opzioni, un bottone "Recupera meteo
mancante" permette di recuperare a posteriori temperatura/umidità sugli
eventi passati che ne sono privi (es. registrati prima che questa
pre-compilazione esistesse), usando l'archivio storico di Open-Meteo.

### Statistiche

KPI in alto (vassoi attivi, raccolto ultimi 30 giorni, resa media/seme),
record personali, grafico di produzione (grammi raccolti e semi usati
affiancati per periodo, di default per anno con un interruttore per
passare al mensile), confronto compatto tra varietà, elenco vassoi con
dettaglio a comparsa, confronto affiancato tra due vassoi. Tutta la
pagina è filtrabile per varietà scorrendo con swipe (o toccando i chip
del filtro): KPI, record e grafico di produzione si ricalcolano sulla
varietà selezionata.

## Stack tecnico

Kotlin, Jetpack Compose (Material 3), architettura MVVM, Hilt, Room (con
Migration esplicite ad ogni cambio di schema), WorkManager per i
promemoria, DataStore Preferences per le impostazioni, OkHttp +
kotlinx.serialization per Open-Meteo (nessuna libreria HTTP di più alto
livello), Navigation-Compose con rotte type-safe. Font Manrope bundlato
localmente (nessuna dipendenza di rete/Google Play Services). Grafici
disegnati con Canvas/Compose, nessuna libreria di charting.

## Build

Il progetto richiede l'Android SDK (compileSdk 35, minSdk 26). Per compilare:

```bash
./gradlew assembleDebug
```

Un workflow GitHub Actions (`.github/workflows/android-build.yml`) compila
automaticamente l'APK debug ad ogni push e lo carica come artifact scaricabile
dalla scheda "Actions" del repository.

## Struttura del progetto

```
core/database      Room: entity, dao, converter, seed
core/di            Moduli Hilt
core/network       Client OkHttp Open-Meteo (geocoding + forecast) e DTO
core/notifications Canale, scheduler WorkManager e worker dei promemoria
core/repository    Repository che orchestrano DAO/rete/DataStore per le ViewModel
feature/template   CRUD template di varietà e step
feature/tray       Vassoi: tab In corso (giorni dalla semina, progresso,
                   stima raccolto)/Raccolti, creazione, modifica,
                   dettaglio/timeline/previsione raccolto
feature/event      Form aggiungi/modifica evento (meteo pre-compilato)
feature/calendar   Vista mensile con filtro per vassoio
feature/stats      Dashboard: KPI in alto, confronto varietà, grafici
                   andamento/produzione mensile, elenco vassoi collassabile
                   con aderenza al piano, confronto vassoi, record
feature/settings   Opzioni: gestione varietà, permesso notifiche, posizione meteo
navigation         Grafo di navigazione Compose (rotte type-safe) + bottom nav
ui, ui/theme       Componenti condivisi (date picker, header compatto, colori/
                   etichette), grafici Canvas (ui/charts) e tema Material 3
                   (colori, tipografia con font Manrope, forme, spaziatura)
```

## Licenza

Tutti i diritti riservati — vedi [`LICENSE`](LICENSE). Il repository è
pubblico per consultazione, ma non è concessa alcuna licenza d'uso, copia
o ridistribuzione del codice senza permesso esplicito. Progetto personale,
non accetto contributi esterni — vedi [`CONTRIBUTING.md`](CONTRIBUTING.md).

## Storico dello sviluppo

Il dettaglio di come l'app è arrivata allo stato attuale, fase per fase, è
in [`CHANGELOG.md`](CHANGELOG.md).
