package com.dedo94.microgreensapp.core.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_NONE
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * weather_daily è solo una cache giornaliera del meteo (vedi KDoc di
 * WeatherDailyEntity): niente da preservare, si ricrea vuota e si
 * ripopola da sola alla prossima chiamata a fetchTodayIfNeeded().
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS weather_daily")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS weather_daily (" +
                "date TEXT NOT NULL PRIMARY KEY, " +
                "fetchedTemperature REAL, " +
                "fetchedHumidity REAL, " +
                "fetchedAt INTEGER NOT NULL, " +
                "locationNameSnapshot TEXT NOT NULL)"
        )
    }
}

/**
 * Introduce le fasi (template_phases) come contenitore degli step di
 * template, e passa gli step di vassoio da "una riga per giorno" a "una riga
 * per occorrenza" (un giorno + un orario, o nessun orario se lo step non ha
 * promemoria). A differenza di MIGRATION_5_6 qui ci sono dati reali da
 * preservare: niente drop/recreate, ogni riga esistente viene letta e
 * riscritta nel nuovo formato.
 *
 * Punti delicati:
 * - `sourceTemplateStepId` sui tray_steps NON è una foreign key applicata
 *   (solo un riferimento informale): l'id dei nuovi template_steps viene
 *   comunque preservato identico per non spezzarne il significato.
 * - `events.trayStepId` È una vera foreign key (ON DELETE SET NULL) verso
 *   tray_steps: la mappatura evento→vecchio-trayStepId viene letta PRIMA di
 *   toccare tray_steps, così la migrazione resta corretta anche se
 *   PRAGMA foreign_keys=OFF risultasse un no-op (SQLite non permette di
 *   cambiarlo dentro una transazione già aperta, e Room esegue le
 *   migrazioni dentro una transazione) — in quel caso DROP TABLE tray_steps
 *   scatenerebbe comunque, implicitamente, il SET NULL su tutti gli eventi
 *   collegati prima ancora di arrivare al passo di ripuntamento.
 * - Uno step con più orari al giorno genera più righe che ereditano tutte
 *   lo stesso status della riga originale: sotto la vecchia granularità
 *   "un giorno" non è mai stato registrato quale occorrenza specifica fosse
 *   stata completata, quindi non si inventa un dato che non esiste.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=OFF")

        // 1. template_phases
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS template_phases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "templateId INTEGER NOT NULL, " +
                "orderIndex INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "durationDays INTEGER, " +
                "FOREIGN KEY(templateId) REFERENCES variety_templates(id) ON DELETE CASCADE)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_template_phases_templateId " +
                "ON template_phases(templateId)"
        )

        // 2. Una fase sintetica "Piano" per ogni template esistente, che
        // ingloba tutti i suoi step attuali senza spostarne gli offset
        // (la fase parte al giorno 0 = giorno di semina, come oggi).
        val phaseIdByTemplateId = mutableMapOf<Long, Long>()
        db.query("SELECT id FROM variety_templates").use { c ->
            val idCol = c.getColumnIndexOrThrow("id")
            while (c.moveToNext()) {
                val templateId = c.getLong(idCol)
                val cv = ContentValues().apply {
                    put("templateId", templateId)
                    put("orderIndex", 0)
                    put("name", "Piano")
                    putNull("durationDays")
                }
                phaseIdByTemplateId[templateId] = db.insert("template_phases", CONFLICT_NONE, cv)
            }
        }

        // 3. template_steps: templateId -> phaseId, drop repeatPerDay,
        // id preservato (sourceTemplateStepId sui vassoi continua a puntare
        // allo step giusto).
        db.execSQL(
            "CREATE TABLE template_steps_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "phaseId INTEGER NOT NULL, " +
                "orderIndex INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "actionType TEXT NOT NULL, " +
                "offsetStartDays INTEGER NOT NULL, " +
                "offsetEndDays INTEGER, " +
                "durationHours INTEGER, " +
                "reminderTimes TEXT NOT NULL, " +
                "instructions TEXT NOT NULL, " +
                "FOREIGN KEY(phaseId) REFERENCES template_phases(id) ON DELETE CASCADE)"
        )
        db.query(
            "SELECT id, templateId, orderIndex, name, actionType, offsetStartDays, " +
                "offsetEndDays, durationHours, reminderTimes, instructions FROM template_steps"
        ).use { c ->
            val idCol = c.getColumnIndexOrThrow("id")
            val templateIdCol = c.getColumnIndexOrThrow("templateId")
            val orderIndexCol = c.getColumnIndexOrThrow("orderIndex")
            val nameCol = c.getColumnIndexOrThrow("name")
            val actionTypeCol = c.getColumnIndexOrThrow("actionType")
            val offsetStartCol = c.getColumnIndexOrThrow("offsetStartDays")
            val offsetEndCol = c.getColumnIndexOrThrow("offsetEndDays")
            val durationCol = c.getColumnIndexOrThrow("durationHours")
            val remindersCol = c.getColumnIndexOrThrow("reminderTimes")
            val instructionsCol = c.getColumnIndexOrThrow("instructions")
            while (c.moveToNext()) {
                val cv = ContentValues().apply {
                    put("id", c.getLong(idCol))
                    put("phaseId", phaseIdByTemplateId.getValue(c.getLong(templateIdCol)))
                    put("orderIndex", c.getInt(orderIndexCol))
                    put("name", c.getString(nameCol))
                    put("actionType", c.getString(actionTypeCol))
                    put("offsetStartDays", c.getInt(offsetStartCol))
                    if (c.isNull(offsetEndCol)) putNull("offsetEndDays") else put("offsetEndDays", c.getInt(offsetEndCol))
                    if (c.isNull(durationCol)) putNull("durationHours") else put("durationHours", c.getInt(durationCol))
                    put("reminderTimes", c.getString(remindersCol))
                    put("instructions", c.getString(instructionsCol))
                }
                db.insert("template_steps_new", CONFLICT_NONE, cv)
            }
        }
        db.execSQL("DROP TABLE template_steps")
        db.execSQL("ALTER TABLE template_steps_new RENAME TO template_steps")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_template_steps_phaseId ON template_steps(phaseId)")

        // 4. Mappatura evento -> vecchio trayStepId, letta PRIMA di toccare
        // tray_steps (vedi KDoc sopra sul perché).
        val oldTrayStepIdByEventId = mutableMapOf<Long, Long>()
        db.query("SELECT id, trayStepId FROM events WHERE trayStepId IS NOT NULL").use { c ->
            val idCol = c.getColumnIndexOrThrow("id")
            val trayStepIdCol = c.getColumnIndexOrThrow("trayStepId")
            while (c.moveToNext()) {
                oldTrayStepIdByEventId[c.getLong(idCol)] = c.getLong(trayStepIdCol)
            }
        }

        // 5. tray_steps: split per occorrenza (una riga per reminderTime, o
        // una sola riga con orario nullo se lo step non ne ha). plannedDate
        // = plannedStartDate (già sempre uguale a plannedEndDate su ogni
        // riga esistente, dato che createTray espande già un giorno a riga).
        db.execSQL(
            "CREATE TABLE tray_steps_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "trayId INTEGER NOT NULL, " +
                "sourceTemplateStepId INTEGER, " +
                "orderIndex INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "actionType TEXT NOT NULL, " +
                "plannedDate TEXT NOT NULL, " +
                "plannedTime TEXT, " +
                "durationHours INTEGER, " +
                "phaseName TEXT NOT NULL, " +
                "phaseOrderIndex INTEGER NOT NULL, " +
                "instructions TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "isAdHoc INTEGER NOT NULL, " +
                "FOREIGN KEY(trayId) REFERENCES trays(id) ON DELETE CASCADE)"
        )
        val firstNewIdByOldStepId = mutableMapOf<Long, Long>()
        db.query(
            "SELECT id, trayId, sourceTemplateStepId, orderIndex, name, actionType, " +
                "plannedStartDate, durationHours, reminderTimes, instructions, status, isAdHoc " +
                "FROM tray_steps"
        ).use { c ->
            val idCol = c.getColumnIndexOrThrow("id")
            val trayIdCol = c.getColumnIndexOrThrow("trayId")
            val sourceStepIdCol = c.getColumnIndexOrThrow("sourceTemplateStepId")
            val orderIndexCol = c.getColumnIndexOrThrow("orderIndex")
            val nameCol = c.getColumnIndexOrThrow("name")
            val actionTypeCol = c.getColumnIndexOrThrow("actionType")
            val dateCol = c.getColumnIndexOrThrow("plannedStartDate")
            val durationCol = c.getColumnIndexOrThrow("durationHours")
            val remindersCol = c.getColumnIndexOrThrow("reminderTimes")
            val instructionsCol = c.getColumnIndexOrThrow("instructions")
            val statusCol = c.getColumnIndexOrThrow("status")
            val isAdHocCol = c.getColumnIndexOrThrow("isAdHoc")
            while (c.moveToNext()) {
                val oldId = c.getLong(idCol)
                val times = runCatching { Json.decodeFromString<List<String>>(c.getString(remindersCol)) }
                    .getOrDefault(emptyList())
                val occurrences: List<String?> = times.ifEmpty { listOf(null) }
                occurrences.forEachIndexed { index, time ->
                    val cv = ContentValues().apply {
                        put("trayId", c.getLong(trayIdCol))
                        if (c.isNull(sourceStepIdCol)) putNull("sourceTemplateStepId") else put("sourceTemplateStepId", c.getLong(sourceStepIdCol))
                        put("orderIndex", c.getInt(orderIndexCol))
                        put("name", c.getString(nameCol))
                        put("actionType", c.getString(actionTypeCol))
                        put("plannedDate", c.getString(dateCol))
                        if (time == null) putNull("plannedTime") else put("plannedTime", time)
                        if (c.isNull(durationCol)) putNull("durationHours") else put("durationHours", c.getInt(durationCol))
                        put("phaseName", "Piano")
                        put("phaseOrderIndex", 0)
                        put("instructions", c.getString(instructionsCol))
                        put("status", c.getString(statusCol))
                        put("isAdHoc", c.getInt(isAdHocCol))
                    }
                    val newId = db.insert("tray_steps_new", CONFLICT_NONE, cv)
                    if (index == 0) firstNewIdByOldStepId[oldId] = newId
                }
            }
        }
        db.execSQL("DROP TABLE tray_steps")
        db.execSQL("ALTER TABLE tray_steps_new RENAME TO tray_steps")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_tray_steps_trayId ON tray_steps(trayId)")

        // 6. Ripunta ogni evento alla riga generata dal suo vecchio step
        // (la prima occorrenza, scelta deterministica), usando la mappatura
        // letta al passo 4 — non una nuova query su events.trayStepId, che
        // potrebbe essere già stata azzerata dal DROP TABLE del passo 5.
        oldTrayStepIdByEventId.forEach { (eventId, oldStepId) ->
            val newStepId = firstNewIdByOldStepId[oldStepId]
            if (newStepId != null) {
                db.execSQL("UPDATE events SET trayStepId = ? WHERE id = ?", arrayOf(newStepId, eventId))
            } else {
                db.execSQL("UPDATE events SET trayStepId = NULL WHERE id = ?", arrayOf(eventId))
            }
        }

        db.query("PRAGMA foreign_key_check").use { c ->
            check(c.count == 0) { "MIGRATION_6_7 ha lasciato foreign key non valide" }
        }
        db.execSQL("PRAGMA foreign_keys=ON")
    }
}
