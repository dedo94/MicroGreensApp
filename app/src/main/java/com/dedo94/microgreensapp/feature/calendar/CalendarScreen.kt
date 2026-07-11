package com.dedo94.microgreensapp.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import com.dedo94.microgreensapp.feature.tray.TrayTimelineEntry
import com.dedo94.microgreensapp.feature.tray.buildTimeline
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.displayColor
import com.dedo94.microgreensapp.ui.displayLabel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onOpenTray: (Long) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val month by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedTrayId by viewModel.selectedTrayId.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val trays by viewModel.trays.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()

    val trayColorById = remember(trays) { trays.associate { it.id to it.displayColor() } }

    val dotsByDate = remember(steps, events, selectedTrayId) {
        buildDotsByDate(steps, events, selectedTrayId)
    }

    val dayEntries = remember(steps, events, selectedDate, selectedTrayId) {
        buildTimeline(
            steps.filter {
                (selectedTrayId == null || it.trayId == selectedTrayId) &&
                    !selectedDate.isBefore(it.plannedStartDate) &&
                    !selectedDate.isAfter(it.plannedEndDate)
            },
            events.filter {
                (selectedTrayId == null || it.trayId == selectedTrayId) && it.eventDate == selectedDate
            },
        )
    }

    Column(Modifier.fillMaxSize()) {
        CompactHeader("Calendario")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = viewModel::goToPreviousMonth) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Mese precedente")
                }
                Text(monthLabel(month), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = viewModel::goToNextMonth) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Mese successivo")
                }
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = selectedTrayId == null,
                        onClick = { viewModel.selectTrayFilter(null) },
                        label = { Text("Tutti") },
                    )
                }
                items(trays, key = { it.id }) { tray ->
                    FilterChip(
                        selected = selectedTrayId == tray.id,
                        onClick = { viewModel.selectTrayFilter(tray.id) },
                        label = { Text(tray.name) },
                    )
                }
            }

            MonthGrid(
                month = month,
                selectedDate = selectedDate,
                dotsByDate = dotsByDate,
                trayColorById = trayColorById,
                onDayClick = viewModel::selectDate,
            )

            Text(
                text = "Il ${selectedDate}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(16.dp),
            )

            if (dayEntries.isEmpty()) {
                Text(
                    text = "Nessun evento in questo giorno.",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(
                        items = dayEntries,
                        key = { entry ->
                            when (entry) {
                                is TrayTimelineEntry.StepEntry -> "step-${entry.step.id}"
                                is TrayTimelineEntry.EventEntry -> "event-${entry.event.id}"
                            }
                        },
                    ) { entry ->
                        val trayId = when (entry) {
                            is TrayTimelineEntry.StepEntry -> entry.step.trayId
                            is TrayTimelineEntry.EventEntry -> entry.event.trayId
                        }
                        val trayName = trays.firstOrNull { it.id == trayId }?.name ?: ""
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onOpenTray(trayId) },
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(trayName, style = MaterialTheme.typography.labelMedium)
                                when (entry) {
                                    is TrayTimelineEntry.StepEntry -> Text(
                                        "${entry.step.name} · ${entry.step.actionType.displayLabel()}"
                                    )

                                    is TrayTimelineEntry.EventEntry -> Text(
                                        "${entry.event.title} · ${entry.event.eventType.displayLabel()}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
}

private fun buildDotsByDate(
    steps: List<TrayStepEntity>,
    events: List<EventEntity>,
    selectedTrayId: Long?,
): Map<LocalDate, Set<Long>> {
    val map = mutableMapOf<LocalDate, MutableSet<Long>>()
    steps.filter { selectedTrayId == null || it.trayId == selectedTrayId }.forEach { step ->
        var day = step.plannedStartDate
        while (!day.isAfter(step.plannedEndDate)) {
            map.getOrPut(day) { mutableSetOf() }.add(step.trayId)
            day = day.plusDays(1)
        }
    }
    events.filter { selectedTrayId == null || it.trayId == selectedTrayId }.forEach { event ->
        map.getOrPut(event.eventDate) { mutableSetOf() }.add(event.trayId)
    }
    return map
}

private fun monthLabel(month: YearMonth): String {
    val name = month.month.getDisplayName(TextStyle.FULL, Locale.ITALIAN)
    return "${name.replaceFirstChar { it.uppercase() }} ${month.year}"
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    dotsByDate: Map<LocalDate, Set<Long>>,
    trayColorById: Map<Long, Color>,
    onDayClick: (LocalDate) -> Unit,
) {
    val firstOfMonth = month.atDay(1)
    val leadingBlanks = firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value
    val gridStart = firstOfMonth.minusDays(leadingBlanks.toLong())
    val weeks = 6

    Column(Modifier.padding(horizontal = 8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            for (dayOfWeek in DayOfWeek.values()) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.ITALIAN),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        for (week in 0 until weeks) {
            Row(Modifier.fillMaxWidth()) {
                for (dayOfWeekIndex in 0 until 7) {
                    val date = gridStart.plusDays((week * 7 + dayOfWeekIndex).toLong())
                    val inCurrentMonth = date.month == month.month
                    val isSelected = date == selectedDate
                    val trayIdsForDay = dotsByDate[date].orEmpty()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                            .clickable { onDayClick(date) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (inCurrentMonth) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                },
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                trayIdsForDay.take(3).forEach { trayId ->
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(trayColorById[trayId] ?: Color.Gray),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
