package com.dedo94.microgreensapp.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStepStatus
import com.dedo94.microgreensapp.feature.tray.TrayTimelineEntry
import com.dedo94.microgreensapp.feature.tray.buildTimeline
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.StepStatusBadge
import com.dedo94.microgreensapp.ui.displayColor
import com.dedo94.microgreensapp.ui.displayLabel
import com.dedo94.microgreensapp.ui.theme.Spacing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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
    val todaySteps by viewModel.todaySteps.collectAsStateWithLifecycle()
    val todayEvents by viewModel.todayEvents.collectAsStateWithLifecycle()

    val today = remember { LocalDate.now() }
    val trayColorById = remember(trays) { trays.associate { it.id to it.displayColor() } }
    val trayNameById = remember(trays) { trays.associate { it.id to it.name } }

    val todayEntries = remember(todaySteps, todayEvents, selectedTrayId) {
        buildTimeline(
            todaySteps.filter { selectedTrayId == null || it.trayId == selectedTrayId },
            todayEvents.filter { selectedTrayId == null || it.trayId == selectedTrayId },
        )
    }

    val dotsByDate = remember(steps, events, selectedTrayId) {
        buildDotsByDate(steps, events, selectedTrayId)
    }

    val dayEntries = remember(steps, events, selectedDate, selectedTrayId) {
        buildTimeline(
            steps.filter {
                (selectedTrayId == null || it.trayId == selectedTrayId) && it.plannedDate == selectedDate
            },
            events.filter {
                (selectedTrayId == null || it.trayId == selectedTrayId) && it.eventDate == selectedDate
            },
        )
    }

    var showMonthView by remember { mutableStateOf(false) }
    var stepPendingQuantityInput by remember { mutableStateOf<TrayStepEntity?>(null) }

    fun proceedMarkDone(step: TrayStepEntity) {
        if (step.actionType == ActionType.HARVEST) {
            stepPendingQuantityInput = step
        } else {
            viewModel.markStepDone(step)
        }
    }

    Column(Modifier.fillMaxSize()) {
        CompactHeader("Calendario")

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.md)) {
            item {
                LazyRow(
                    modifier = Modifier.padding(vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
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
            }

            item {
                Text(
                    text = "Oggi",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = Spacing.sm),
                )
            }
            if (todayEntries.isEmpty()) {
                item {
                    Text(
                        text = "Nessuna azione pianificata per oggi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(
                    items = todayEntries,
                    key = { entry ->
                        when (entry) {
                            is TrayTimelineEntry.StepEntry -> "today-step-${entry.step.id}"
                            is TrayTimelineEntry.EventEntry -> "today-event-${entry.event.id}"
                        }
                    },
                ) { entry ->
                    val trayId = when (entry) {
                        is TrayTimelineEntry.StepEntry -> entry.step.trayId
                        is TrayTimelineEntry.EventEntry -> entry.event.trayId
                    }
                    TodayEntryCard(
                        entry = entry,
                        trayName = trayNameById[trayId] ?: "",
                        trayColor = trayColorById[trayId] ?: MaterialTheme.colorScheme.outline,
                        onMarkDone = {
                            if (entry is TrayTimelineEntry.StepEntry) proceedMarkDone(entry.step)
                        },
                        onOpenTray = { onOpenTray(trayId) },
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMonthView = !showMonthView }
                        .padding(vertical = Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Mese", style = MaterialTheme.typography.titleMedium)
                    Icon(
                        imageVector = if (showMonthView) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (showMonthView) "Comprimi" else "Espandi",
                    )
                }
            }

            if (showMonthView) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                }
                item {
                    MonthGrid(
                        month = month,
                        selectedDate = selectedDate,
                        dotsByDate = dotsByDate,
                        trayColorById = trayColorById,
                        onDayClick = viewModel::selectDate,
                    )
                }

                if (selectedDate != today) {
                    item {
                        Text(
                            text = "Il ${selectedDate}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = Spacing.sm),
                        )
                    }
                    if (dayEntries.isEmpty()) {
                        item {
                            Text(
                                text = "Nessun evento in questo giorno.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(
                            items = dayEntries,
                            key = { entry ->
                                when (entry) {
                                    is TrayTimelineEntry.StepEntry -> "day-step-${entry.step.id}"
                                    is TrayTimelineEntry.EventEntry -> "day-event-${entry.event.id}"
                                }
                            },
                        ) { entry ->
                            val trayId = when (entry) {
                                is TrayTimelineEntry.StepEntry -> entry.step.trayId
                                is TrayTimelineEntry.EventEntry -> entry.event.trayId
                            }
                            val trayName = trayNameById[trayId] ?: ""
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.xs)
                                    .clickable { onOpenTray(trayId) }
                                    .animateItem(),
                            ) {
                                Column(Modifier.padding(Spacing.sm)) {
                                    Text(trayName, style = MaterialTheme.typography.labelMedium)
                                    when (entry) {
                                        is TrayTimelineEntry.StepEntry -> Text(stepLabel(entry.step))

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

            item { Spacer(Modifier.height(Spacing.md)) }
        }
    }

    stepPendingQuantityInput?.let { step ->
        var quantityText by remember(step.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { stepPendingQuantityInput = null },
            title = { Text("Registra il raccolto") },
            text = {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Quantità raccolta (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.markStepDone(
                        step = step,
                        quantityValue = quantityText.toDoubleOrNull(),
                        quantityUnit = "g",
                    )
                    stepPendingQuantityInput = null
                }) { Text("Conferma") }
            },
            dismissButton = {
                TextButton(onClick = { stepPendingQuantityInput = null }) { Text("Annulla") }
            },
        )
    }
}

@Composable
private fun TodayEntryCard(
    entry: TrayTimelineEntry,
    trayName: String,
    trayColor: Color,
    onMarkDone: () -> Unit,
    onOpenTray: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenTray)
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(trayColor),
            )
            Spacer(Modifier.width(Spacing.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    text = trayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                when (entry) {
                    is TrayTimelineEntry.StepEntry -> Text(
                        text = stepLabel(entry.step),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    is TrayTimelineEntry.EventEntry -> Text(
                        text = "${entry.event.title} · ${entry.event.eventType.displayLabel()}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (entry is TrayTimelineEntry.StepEntry) {
                if (entry.step.status == TrayStepStatus.PENDING) {
                    IconButton(onClick = onMarkDone) {
                        Icon(Icons.Outlined.Check, contentDescription = "Segna come fatto")
                    }
                } else {
                    StepStatusBadge(entry.step.status)
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
        map.getOrPut(step.plannedDate) { mutableSetOf() }.add(step.trayId)
    }
    events.filter { selectedTrayId == null || it.trayId == selectedTrayId }.forEach { event ->
        map.getOrPut(event.eventDate) { mutableSetOf() }.add(event.trayId)
    }
    return map
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Include l'orario quando presente: distingue due occorrenze dello stesso step nello stesso giorno (es. sciacquo mattina/sera). */
private fun stepLabel(step: TrayStepEntity): String {
    val time = step.plannedTime?.format(timeFormatter)?.let { " · $it" } ?: ""
    return "${step.name} · ${step.actionType.displayLabel()}$time"
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

    Column {
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
