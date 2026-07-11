# MicroGreensApp

App Android nativa (Kotlin + Jetpack Compose) per tenere traccia di tutto il
processo di coltivazione dei microgreens, organizzato per vassoi: template di
fasi personalizzabili, calendario unico degli eventi, notifiche, dati
meteo/alba-tramonto e statistiche. Uso personale, dati 100% locali (Room), nessun account.

Sviluppato in 6 fasi iniziali, riassunte qui sotto, seguite da miglioramenti
successivi e da un redesign completo dell'interfaccia.

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
- [x] **Fase 5** — Foto: scatto con fotocamera di sistema o selezione dalla
      galleria (Photo Picker, nessun permesso storage richiesto), storage
      privato dell'app (`filesDir/photos`, eliminato alla disinstallazione),
      galleria orizzontale sul vassoio e sul singolo evento con
      visualizzatore a schermo intero ed eliminazione. Build verificata con
      successo su GitHub Actions.
- [x] **Fase 6** — Statistiche: per vassoio (semi, acqua totale, resa
      raccolto, resa per grammo di seme, efficienza idrica, durata effettiva
      vs pianificata, condizioni medie registrate); per varietà (cicli,
      tasso di successo, resa/durata medie); produzione mensile totale;
      record personali (raccolto più abbondante, miglior resa/seme, ciclo
      più breve). Calcolate in Kotlin dai dati già presenti, nessuna nuova
      dipendenza. Build verificata con successo su GitHub Actions.

Tutte e 6 le fasi del piano sono complete.

## Dopo le 6 fasi

Miglioramenti e correzioni successivi al piano iniziale, dall'uso reale
dell'app:

- **Step giornalieri**: uno step di template su più giorni (es. "Crescita"
  giorni 5-10) viene copiato nel vassoio come un task per ciascun giorno,
  confermabile singolarmente invece che con un'unica spunta per l'intera
  fase. Risolta anche una spunta che appariva duplicata nella timeline.
- **Annulla completamento**: uno step segnato fatto/saltato per errore si
  può riportare a "da fare"; confermare uno step pianificato per un giorno
  futuro chiede prima conferma.
- **Registrazione del raccolto**: segnare come fatto uno step di Raccolta o
  Irrigazione chiede la quantità (grammi/ml), che prima non aveva mai un
  punto in cui essere inserita e non contribuiva alle statistiche.
- **Modifica vassoio**: nome, quantità semi, substrato e note si possono
  modificare dopo la creazione (la data di semina resta fissa: da essa
  dipendono le date già calcolate degli step pianificati).
- **Statistiche potenziate**: filtro per varietà, grafico dell'andamento
  della resa nel tempo, produzione mensile come grafico a barre, confronto
  affiancato di due vassoi. Grafici scritti con Canvas/Compose, nessuna
  libreria di charting aggiunta.
- **Lista vassoi a tab**: In corso / Raccolti, invece di un unico elenco
  misto. Lo stato "Abbandonato" è stato rimosso: un vassoio abbandonato si
  elimina e basta.
- **Previsione raccolto**: nel dettaglio di un vassoio in corso, se esiste
  almeno un ciclo già raccolto della stessa varietà, una stima basata sui
  semi usati in questo vassoio × resa media per grammo di seme storica.
- **Restyling**: tema verde/allegro sempre visibile (il dynamic color di
  Android 12+ lo sostituiva con i colori dello sfondo del telefono), forme
  più morbide in tutta l'app, fix di un paio di FAB che coprivano l'ultimo
  elemento delle liste.
- **Aggiornamenti senza perdita dati**: keystore di debug fissa invece di
  quella rigenerata ad ogni build di CI, così il nuovo APK si installa
  sopra quello precedente senza dover disinstallare.
- **Revisione UX**: rimossa la feature foto (poco usata, occupava spazio in
  ogni schermata e portava con sé la dipendenza Coil e il FileProvider);
  intestazioni compatte sulle 4 schede principali al posto delle TopAppBar;
  gestione varietà spostata in Opzioni con una voce esplicita (al posto di
  un'icona a foglia poco chiara); nel dettaglio vassoio il FAB flottante è
  sostituito da una card "+" in fondo alla timeline; la scheda
  "Impostazioni" è stata rinominata "Opzioni".
- **Fix perdita dati e restyling varietà**: il seeding del template
  "Girasole" precaricato non dipende più dal Callback.onCreate di Room (che
  non si rieseguiva in modo affidabile) ma viene rieseguito ad ogni avvio
  dell'app in modo idempotente, così torna sempre disponibile. Rimosso anche
  `fallbackToDestructiveMigration()`, che cancellava l'intero database ad
  ogni cambio di schema: da qui in avanti i cambi di schema richiedono una
  Migration esplicita. Le schermate "Gestisci varietà" (lista e dettaglio),
  rimaste indietro rispetto al resto della revisione UX, ora hanno la
  freccia indietro nella TopAppBar e la card "+" in fondo alla lista al
  posto del FAB flottante.
- **Header compatti ovunque**: l'intestazione compatta, prima solo sulle 4
  schede principali, ora sostituisce la TopAppBar (64dp) anche in tutte le
  schermate raggiunte "andando avanti" (Nuovo/Modifica vassoio, Nuovo/
  Modifica evento, dettaglio vassoio, Gestisci varietà), con freccia
  indietro e azioni integrate nello stesso componente compatto.
- **Fix crash all'avvio**: `fallbackToDestructiveMigrationOnDowngrade()` da
  solo copriva solo i downgrade, non gli aggiornamenti da uno schema
  pre-versione-5 (il DB è passato per le versioni 1→2→3→4→5 nel tempo,
  sempre gestite finora con la fallback distruttiva generica). Chi aveva
  ancora un DB locale fermo a una di quelle versioni otteneva un crash
  immediato all'avvio. Aggiunto `fallbackToDestructiveMigrationFrom(1, 2, 3,
  4)` per coprire quel salto storico una volta per tutte.

## Redesign UI

Restyling completo in chiave "fresh & organic" (Material 3, coltivazione/
natura/freschezza), fatto in più fasi — una PR per fase, ciascuna
autoconclusiva:

- **Fondamenta del tema**: schema colori Material 3 completo
  (light + dark, tutti i ruoli) derivato da 5 colori seed (verde foglia
  `#4C956C`, verde salvia `#7A9E7E`, accento sole `#E8A33D`, superfici
  chiarissima verdastra in light/verde scurissima in dark);
  `onPrimary`/`onSecondary`/`onTertiary` in inchiostro scuro invece che
  bianco per rispettare il contrasto minimo WCAG AA con questi seed a
  luminosità media. Font Manrope (variabile, licenza SIL OFL, bundlato
  come file statico in `res/font/`, nessuna dipendenza di rete/Google Play
  Services) con una scala tipografica pesata (display/headline in
  bold-semibold, title semibold, body regular, label medium). Corner
  radius delle Card nel range 16-20dp. Nuovo token di spaziatura da 8dp
  (`ui/theme/Spacing.kt`). Palette dei pallini identificativi dei vassoi
  attenuata per stare in armonia con le nuove superfici verdi.
- **Icone**: tutte le icone (17 in totale) passate da Material Icons
  Filled a Outlined, coerente con lo stile più pulito del resto.
- **Dashboard vassoi**: la tab "In corso" mostra ora giorni dalla semina,
  barra di progresso e giorni al raccolto stimato per ogni vassoio con un
  piano di coltivazione (nessun badge di fase testuale, solo numeri).
- **Dettaglio vassoio, form, Statistiche, Calendario, schermate minori**
  (Opzioni, Varietà, evento): spaziatura sui token `Spacing`, card con le
  nuove forme/colori, empty state con icona invece di solo testo,
  transizioni leggere (`animateItem`/`animateContentSize`) su comparsa e
  aggiornamento delle card.

## Dopo il redesign

- **Substrati semplificati**: la scelta del substrato in "Nuovo vassoio" è
  ridotta a Idroponica (default), Terriccio e Altro — le altre opzioni
  (fibra di cocco, canapa, carta assorbente, perlite/vermiculite) sono
  state rimosse. Un vassoio già salvato con uno di quei valori non
  crasha: viene letto come "Altro".
- **Nomi botanici delle varietà**: il girasole precaricato ora ha come tipo
  pianta "Helianthus Annuus" (era una descrizione generica); aggiunta una
  seconda varietà precaricata, "Piselli" (Pisum Sativum), con un piano di
  coltivazione di partenza sullo stesso schema del girasole — completamente
  modificabile da "Gestisci varietà".
- **Notifiche on/off**: in Opzioni, un interruttore attiva/disattiva i
  promemoria in qualsiasi momento, indipendentemente dal permesso di
  sistema. Disattivandolo si cancellano subito tutti i promemoria già in
  coda; riattivandolo si ripianificano quelli dei vassoi in corso.

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
feature/tray       Vassoi: tab In corso (giorni dalla semina, progresso,
                   stima raccolto)/Raccolti, creazione, modifica,
                   dettaglio/timeline/previsione raccolto
feature/event      Form aggiungi/modifica evento (meteo pre-compilato)
feature/calendar   Vista mensile con filtro per vassoio
feature/stats      Dashboard: per vassoio, per varietà (con filtro), grafici
                   andamento/produzione mensile, confronto vassoi, record
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
