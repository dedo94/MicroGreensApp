# Changelog

Storico dello sviluppo di MicroGreensApp, in ordine cronologico. Per la
descrizione dell'app così com'è oggi vedi [`README.md`](README.md).

## Fase 1 — Scaffold e template

Scaffold progetto (Gradle/Kotlin/Compose/Hilt/Room) e CRUD completo dei
template di varietà (metadati + step riordinabili), con il template
"Girasole" precaricato al primo avvio.

## Fase 2 — Vassoi, eventi, calendario

Vassoi con snapshot del piano dal template scelto, timeline unificata di
step pianificati + eventi liberi per vassoio (segna fatto/salta/modifica
senza toccare il template), calendario mensile con filtro per vassoio,
bottom nav (Calendario/Vassoi/Statistiche/Impostazioni).

## Fase 3 — Notifiche

Promemoria locali con WorkManager derivati dagli step pianificati (orari di
sciacquo/irrigazione/raccolta ecc.), ricalcolati automaticamente ad ogni
modifica del piano di un vassoio, con tap sulla notifica che apre il
vassoio corrispondente. Richiesta permesso notifiche dalle Impostazioni.

## Fase 4 — Meteo

Meteo Open-Meteo: ricerca e impostazione posizione dalle Impostazioni
(cambiabile in qualsiasi momento), temperatura/umidità/alba-tramonto del
giorno pre-compilate nel form evento ma sempre sovrascrivibili, con cache
giornaliera locale. Chiamate dirette con OkHttp + kotlinx.serialization.
(Alba/tramonto rimossi in seguito, vedi più sotto: non erano mai mostrati
in nessuna schermata.)

## Fase 5 — Foto

Scatto con fotocamera di sistema o selezione dalla galleria (Photo
Picker, nessun permesso storage richiesto), storage privato dell'app
(`filesDir/photos`, eliminato alla disinstallazione), galleria orizzontale
sul vassoio e sul singolo evento con visualizzatore a schermo intero ed
eliminazione. (Rimossa in seguito, vedi "Revisione UX" più sotto.)

## Fase 6 — Statistiche

Statistiche: per vassoio (semi, acqua totale, resa raccolto, resa per
grammo di seme, efficienza idrica, durata effettiva vs pianificata,
condizioni medie registrate); per varietà (cicli, tasso di successo,
resa/durata medie); produzione mensile totale; record personali (raccolto
più abbondante, miglior resa/seme, ciclo più breve). Calcolate in Kotlin
dai dati già presenti, nessuna nuova dipendenza.

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
- **Rimozione alba/tramonto**: il dato veniva recuperato da Open-Meteo e
  salvato in cache ma non era mai stato mostrato in nessuna schermata.
  Restano solo temperatura/umidità, effettivamente usate nel form evento e
  nelle statistiche. La riga "Condizioni medie" in Statistiche è ora sempre
  visibile (con "—" per i valori mancanti) invece di sparire quando
  temperatura/umidità non sono registrate.
- **Colore per varietà**: i pallini identificativi in calendario e lista
  vassoi erano assegnati per vassoio (ordine di creazione), quindi due
  vassoi della stessa varietà avevano colori diversi. Ora il colore è
  derivato dalla varietà e resta lo stesso per tutti i vassoi che la
  condividono.
- **Bottone di conferma in basso**: nei 4 form principali (Nuovo/Modifica
  vassoio, Gestisci varietà, Nuovo/Modifica evento) il salvataggio passa
  dall'icona ✓ in alto, scomoda da raggiungere con le dita, a un bottone
  largo quanto lo schermo fissato in basso.
- **Ridisegno pagina Statistiche**: gerarchia visiva al posto del muro di
  testo iniziale. Riga di KPI in alto (vassoi attivi, raccolto ultimi 30
  giorni, resa media/seme) subito visibile aprendo la tab; confronto tra
  varietà in un'unica tabella compatta invece di una card per varietà;
  elenco vassoi collassato di default (dettaglio completo al tap) con un
  badge di aderenza al piano (% step fatti vs saltati, dato già presente
  ma prima mai mostrato); confronto tra due vassoi reso una sezione
  richiudibile secondaria.
- **Raccolto → stato automatico**: registrare un raccolto (da uno step
  pianificato o da un evento libero) porta subito il vassoio a "Raccolto",
  cancellando i promemoria residui — prima andava cambiato a mano dal menu
  del dettaglio vassoio.
- **Tab "Raccolti" con l'esito del ciclo**: mostra ora resa totale e durata
  effettiva per ogni vassoio raccolto, non solo nome/varietà/data semina —
  è l'informazione che conta per confrontare i cicli passati.
- **Ridisegno timeline vassoio**: step pianificati ed eventi liberi hanno
  ora un'icona diversa per distinguerli a colpo d'occhio; lo stato di uno
  step è un badge colorato (Fatto/Saltato) invece di testo in fondo alla
  riga; la timeline è raggruppata per data (Oggi/Ieri/Domani/data) invece
  di essere un'unica lista piatta; le azioni secondarie (modifica, salta,
  elimina, annulla) sono in un menu invece di icone sempre visibili,
  lasciando in vista solo l'azione primaria. La card info del vassoio
  mostra anche il badge di aderenza al piano.
- **Gesture di swipe**: in Vassoi si passa dalla tab "In corso" a
  "Raccolti" anche scorrendo con il dito, non solo toccando la tab
  (`TabRow` + `HorizontalPager` sincronizzati). In Statistiche lo stesso
  vale per le varietà: scorrere cambia la varietà selezionata come toccare
  i chip del filtro — il filtro non è più uno stato separato nel
  ViewModel, è direttamente la pagina corrente dello swipe.
- **Piano di coltivazione dei piselli corretto**: ammollo portato da 12 a
  10 ore; il trasferimento nel vassoio avviene subito il giorno 1 (non più
  dopo la prevenzione muffa) con l'accortezza di non far sovrapporre i
  semi e l'indicazione di un peso sopra per aiutare la crescita; la
  prevenzione muffa (sciacquo 2 volte al giorno) copre i giorni 1-3 nel
  vassoio stesso; la raccolta è una finestra di giorni 9-11, quando i
  germogli raggiungono circa 20cm, non più un giorno fisso; la
  conservazione resta identica a quella del girasole. La correzione si
  applica anche a chi ha già il template "Piselli" installato (una tantum,
  solo se non è stato modificato a mano), senza toccare i vassoi già creati.
- **Statistiche filtrate per varietà anche in alto**: KPI, record personali
  e produzione mensile restavano sempre gli stessi scorrendo tra le
  varietà (mostravano solo i dati globali). Ora ogni pagina dello swipe
  mostra i KPI/record/produzione calcolati sui soli vassoi di quella
  varietà (calcolo estratto in `StatsRepository` e riusato sia per il
  totale sia per ogni varietà).
- **Allineamento riga "Gestisci varietà" in Opzioni**: icona e freccia
  erano allineate in alto rispetto al testo su due righe (comportamento
  di default del componente `ListItem` di Material 3 con testo
  secondario); ora sono centrate verticalmente rispetto all'intero blocco
  di testo.
- **Bottone "+" fisso in "Gestisci varietà"**: prima appariva subito sotto
  l'ultima varietà in elenco, quindi in alto nello schermo se le varietà
  erano poche. Ora è sempre in fondo alla schermata, sia con elenco vuoto
  che pieno.
- **Ristrutturazione Calendario, sezione "Oggi"**: prima la prima cosa
  visibile aprendo l'app (Calendario è la tab di partenza) era la griglia
  del mese intero, con gli eventi di oggi in fondo dopo uno scroll e senza
  poterli segnare fatti direttamente. Ora "Oggi" è la prima sezione,
  azionabile (spunta diretta per gli step, con il dialogo quantità per la
  raccolta), aggregata su tutti i vassoi; la griglia del mese è dietro un
  toggle "Mese", per quando si vuole sfogliare il calendario invece di
  agire su oggi. `StepStatusBadge`, prima privato di `TrayDetailScreen`,
  è stato estratto in un componente condiviso (`ui/StepStatusBadge.kt`) e
  riusato qui.
- **Rimossa la quantità obbligatoria per l'irrigazione**: in pratica
  impossibile da misurare (nebulizzazione sui semi nelle prime fasi, poi
  acqua versata nel sotto-vassoio), quindi segnare fatto uno step di
  irrigazione non chiede più una quantità — resta richiesta solo per la
  raccolta, l'unica effettivamente misurabile. L'"acqua totale"/
  "efficienza idrica" nelle statistiche mostrano "—" invece di un
  fuorviante 0 quando non c'è nessuna quantità registrata.
- **Fix: temperatura/umidità mancanti anche con la posizione impostata**:
  il meteo veniva pre-compilato solo aggiungendo un evento libero dal form
  dedicato — mai segnando fatto uno step pianificato, che è il modo con
  cui nasce la maggior parte degli eventi nell'uso quotidiano (ancora di
  più dopo la sezione "Oggi"). `TrayRepository.markStepDone()` ora
  recupera anche lui il meteo del giorno e lo allega all'evento.
- **Recupero meteo storico mancante**: gli eventi passati creati prima del
  fix precedente sono rimasti senza temperatura/umidità. Un bottone
  "Recupera meteo mancante" in Opzioni (visibile solo con una posizione
  impostata) cerca tutti gli eventi senza meteo, recupera i dati storici
  di quelle date in un'unica chiamata all'archivio Open-Meteo
  (`archive-api.open-meteo.com`) e li allega. Innescato manualmente
  invece che in automatico all'avvio, per tenere una chiamata di rete non
  banale visibile e controllabile invece che silenziosa in background.
  Verificato funzionante su dispositivo reale.
- **Layout del blocco Meteo in Opzioni più compatto**: posizione,
  ricerca città e recupero meteo storico erano tutti sempre visibili,
  con testo esplicativo su più righe — troppo ingombrante per
  un'impostazione usata di rado. Ora la posizione è una riga compatta
  (icona + nome + sottotitolo) che si espande solo toccandola per
  cercare/cambiare città; il recupero meteo storico è un'altra riga
  compatta con lo stato (in corso/risultato) come sottotitolo invece che
  paragrafi separati.
- **Grafico di produzione con semi usati, e vista annuale**: il grafico
  "Produzione mensile" nelle Statistiche mostrava solo i grammi raccolti,
  senza valori/unità visibili sulle barre. Ora mostra due barre per
  periodo (semi e raccolto, in quest'ordine, con etichetta in grammi
  sopra ciascuna), aggregate ognuna sulla propria data naturale — i semi
  per mese/anno di semina, il raccolto per mese/anno di raccolto, senza
  cercare di far coincidere i due cicli. Di default mostra i totali per
  anno invece che per mese — molto più leggibile su più cicli di
  coltivazione — con un interruttore testuale "Anno"/"Mese" in linea col
  titolo della sezione per passare alla vista mensile quando serve.
