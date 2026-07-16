package com.dedo94.microgreensapp.feature.tray

import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import java.time.LocalDate
import java.time.LocalTime

sealed interface TrayTimelineEntry {
    val date: LocalDate

    data class StepEntry(val step: TrayStepEntity) : TrayTimelineEntry {
        override val date: LocalDate get() = step.plannedDate
    }

    data class EventEntry(val event: EventEntity) : TrayTimelineEntry {
        override val date: LocalDate get() = event.eventDate
    }
}

/**
 * Gli eventi con trayStepId non nullo sono l'"eco" nel diario generata da
 * markStepDone: lo step stesso (già visibile come StepEntry, ora DONE) li
 * rappresenta già, quindi non vanno mostrati una seconda volta come riga
 * separata. Restano comunque in tabella per le statistiche.
 *
 * A parità di data, l'ordinamento per orderIndex+orario è esplicito (non
 * incidentale): due righe possono ora condividere sia la data che
 * l'orderIndex (stesso step, due orari nello stesso giorno).
 */
fun buildTimeline(steps: List<TrayStepEntity>, events: List<EventEntity>): List<TrayTimelineEntry> =
    (steps.map(TrayTimelineEntry::StepEntry) + events.filter { it.trayStepId == null }.map(TrayTimelineEntry::EventEntry))
        .sortedWith(
            compareBy(
                { it.date },
                { (it as? TrayTimelineEntry.StepEntry)?.step?.orderIndex ?: 0 },
                { (it as? TrayTimelineEntry.StepEntry)?.step?.plannedTime ?: LocalTime.MIN },
            )
        )
