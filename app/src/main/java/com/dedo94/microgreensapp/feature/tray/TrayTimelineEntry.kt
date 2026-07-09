package com.dedo94.microgreensapp.feature.tray

import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import java.time.LocalDate

sealed interface TrayTimelineEntry {
    val date: LocalDate

    data class StepEntry(val step: TrayStepEntity) : TrayTimelineEntry {
        override val date: LocalDate get() = step.plannedStartDate
    }

    data class EventEntry(val event: EventEntity) : TrayTimelineEntry {
        override val date: LocalDate get() = event.eventDate
    }
}

fun buildTimeline(steps: List<TrayStepEntity>, events: List<EventEntity>): List<TrayTimelineEntry> =
    (steps.map(TrayTimelineEntry::StepEntry) + events.map(TrayTimelineEntry::EventEntry))
        .sortedBy { it.date }
