package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.entity.DailyRecord
import com.example.ui.components.FarmDatePickerDialog
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ForestGreenPrimaryContainer
import com.example.ui.theme.HarvestAmberTertiary
import com.example.ui.theme.HarvestAmberTertiaryContainer
import com.example.ui.theme.SproutSecondary
import com.example.ui.theme.SproutSecondaryContainer
import com.example.util.DateUtils

enum class HistoryFilter {
    ALL,
    TODAY,
    YESTERDAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    CUSTOM
}

@Composable
fun RecordsHistoryScreen(
    records: List<DailyRecord>,
    onEditRecord: (Long) -> Unit,
    onDeleteRecord: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var customStartDate by remember { mutableStateOf(DateUtils.getDaysAgoDateString(7)) }
    var customEndDate by remember { mutableStateOf(DateUtils.getCurrentDateString()) }

    var recordToDelete by remember { mutableStateOf<DailyRecord?>(null) }
    var expandedRecordId by remember { mutableStateOf<Long?>(null) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }

    val todayStr = DateUtils.getCurrentDateString()
    val yesterdayStr = DateUtils.getYesterdayDateString()
    val sevenDaysAgoStr = DateUtils.getDaysAgoDateString(7)
    val thirtyDaysAgoStr = DateUtils.getDaysAgoDateString(30)

    val filteredRecords = records.filter { record ->
        val matchesDate = when (selectedFilter) {
            HistoryFilter.ALL -> true
            HistoryFilter.TODAY -> record.recordDateString == todayStr
            HistoryFilter.YESTERDAY -> record.recordDateString == yesterdayStr
            HistoryFilter.LAST_7_DAYS -> record.recordDateString >= sevenDaysAgoStr
            HistoryFilter.LAST_30_DAYS -> record.recordDateString >= thirtyDaysAgoStr
            HistoryFilter.CUSTOM -> record.recordDateString in customStartDate..customEndDate
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            record.recordDateString.contains(searchQuery.trim(), ignoreCase = true) ||
                    DateUtils.formatDateForDisplay(record.recordDateString).contains(searchQuery.trim(), ignoreCase = true)
        }
        matchesDate && matchesSearch
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.records_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )

            Spacer(Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_search_records")
            )

            Spacer(Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.ALL,
                        onClick = { selectedFilter = HistoryFilter.ALL },
                        label = { Text(stringResource(R.string.filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.TODAY,
                        onClick = { selectedFilter = HistoryFilter.TODAY },
                        label = { Text(stringResource(R.string.filter_today)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.YESTERDAY,
                        onClick = { selectedFilter = HistoryFilter.YESTERDAY },
                        label = { Text(stringResource(R.string.filter_yesterday)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.LAST_7_DAYS,
                        onClick = { selectedFilter = HistoryFilter.LAST_7_DAYS },
                        label = { Text(stringResource(R.string.filter_last_7_days)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.LAST_30_DAYS,
                        onClick = { selectedFilter = HistoryFilter.LAST_30_DAYS },
                        label = { Text(stringResource(R.string.filter_last_30_days)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.CUSTOM,
                        onClick = {
                            selectedFilter = HistoryFilter.CUSTOM
                            showCustomStartPicker = true
                        },
                        label = { Text(stringResource(R.string.filter_custom)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                    )
                }
            }

            if (selectedFilter == HistoryFilter.CUSTOM) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${DateUtils.formatDateForDisplay(customStartDate)}  →  ${DateUtils.formatDateForDisplay(customEndDate)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ForestGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { showCustomStartPicker = true }) {
                        Text("Change Range")
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.records_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        val isExpanded = expandedRecordId == record.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedRecordId = if (isExpanded) null else record.id
                                }
                                .testTag("record_card_${record.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header: Date and Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = DateUtils.formatDateForDisplay(record.recordDateString),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary
                                        )
                                        Text(
                                            text = DateUtils.formatTimeForDisplay(record.recordTimeMillis),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onEditRecord(record.id) },
                                            modifier = Modifier.size(36.dp).testTag("btn_edit_${record.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = ForestGreenPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { recordToDelete = record },
                                            modifier = Modifier.size(36.dp).testTag("btn_delete_${record.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Quick Overview Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Total Live Birds
                                    Column {
                                        Text(
                                            text = stringResource(R.string.dashboard_total_live),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${record.totalLiveBirds}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary
                                        )
                                    }

                                    // Eggs
                                    Column {
                                        Text(
                                            text = stringResource(R.string.dashboard_eggs_today),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${record.eggsCollected}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = HarvestAmberTertiary
                                        )
                                    }

                                    // Deaths
                                    Column {
                                        Text(
                                            text = stringResource(R.string.dashboard_total_deaths_today),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${record.totalDeaths}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (record.totalDeaths > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Expanded details
                                if (isExpanded) {
                                    Spacer(Modifier.height(14.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "Adults: ${record.openingAdults} opening + ${record.adultsAdded} added - ${record.adultDeaths} dead - ${record.adultsSold} sold = ${record.closingAdults} closing",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Chicks: ${record.openingChicks} opening + ${record.chicksAdded} added - ${record.chickDeaths} dead - ${record.chicksSold} sold = ${record.closingChicks} closing",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            val mortality = record.dailyMortalityRatePercent?.let { String.format(java.util.Locale.US, "%.2f%%", it) } ?: "N/A"
                                            Text(
                                                text = "Mortality Rate: $mortality",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (record.notes.isNotBlank()) {
                                                Text(
                                                    text = "Notes: ${record.notes}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (record.healthObservations.isNotBlank()) {
                                                Text(
                                                    text = "Health: ${record.healthObservations}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (record.treatments.isNotBlank()) {
                                                Text(
                                                    text = "Treatments: ${record.treatments}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Confirm Delete Dialog
    if (recordToDelete != null) {
        val target = recordToDelete!!
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.confirm_delete_message,
                        DateUtils.formatDateForDisplay(target.recordDateString)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = target.id
                        recordToDelete = null
                        onDeleteRecord(id)
                    },
                    modifier = Modifier.testTag("btn_confirm_delete")
                ) {
                    Text(
                        stringResource(R.string.dialog_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    if (showCustomStartPicker) {
        FarmDatePickerDialog(
            initialDateMillis = DateUtils.parseDateStringToMillis(customStartDate),
            onDateSelected = { selected ->
                customStartDate = selected
                showCustomStartPicker = false
                showCustomEndPicker = true
            },
            onDismiss = { showCustomStartPicker = false }
        )
    }

    if (showCustomEndPicker) {
        FarmDatePickerDialog(
            initialDateMillis = DateUtils.parseDateStringToMillis(customEndDate),
            onDateSelected = { selected ->
                customEndDate = selected
                showCustomEndPicker = false
            },
            onDismiss = { showCustomEndPicker = false }
        )
    }
}
