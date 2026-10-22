package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.entity.Farm
import com.example.ui.components.FarmDatePickerDialog
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.StatCard
import com.example.ui.components.TrendLineChart
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ForestGreenPrimaryContainer
import com.example.ui.theme.HarvestAmberTertiary
import com.example.ui.theme.HarvestAmberTertiaryContainer
import com.example.ui.theme.SproutSecondary
import com.example.ui.theme.SproutSecondaryContainer
import com.example.ui.viewmodel.ReportPeriod
import com.example.ui.viewmodel.ReportsViewModel
import com.example.util.DateUtils

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    farm: Farm?,
    onExportReportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.reports_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )

                    OutlinedButton(
                        onClick = onExportReportCsv,
                        modifier = Modifier.testTag("btn_export_reports_csv")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.export_report_csv), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Period Selector Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedPeriod == ReportPeriod.TODAY,
                            onClick = { viewModel.selectPeriod(ReportPeriod.TODAY) },
                            label = { Text(stringResource(R.string.filter_today)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedPeriod == ReportPeriod.LAST_7_DAYS,
                            onClick = { viewModel.selectPeriod(ReportPeriod.LAST_7_DAYS) },
                            label = { Text(stringResource(R.string.filter_last_7_days)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedPeriod == ReportPeriod.LAST_30_DAYS,
                            onClick = { viewModel.selectPeriod(ReportPeriod.LAST_30_DAYS) },
                            label = { Text(stringResource(R.string.filter_last_30_days)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedPeriod == ReportPeriod.ALL_TIME,
                            onClick = { viewModel.selectPeriod(ReportPeriod.ALL_TIME) },
                            label = { Text(stringResource(R.string.filter_all)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedPeriod == ReportPeriod.CUSTOM,
                            onClick = { showCustomStartPicker = true },
                            label = { Text(stringResource(R.string.filter_custom)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimaryContainer)
                        )
                    }
                }
            }

            if (uiState.selectedPeriod == ReportPeriod.CUSTOM) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${DateUtils.formatDateForDisplay(uiState.customStartDate)}  →  ${DateUtils.formatDateForDisplay(uiState.customEndDate)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestGreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { showCustomStartPicker = true }) {
                            Text("Change Range")
                        }
                    }
                }
            }

            // Key Metrics Section: Egg Production
            item {
                Text(
                    text = "Egg Production",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.stat_eggs_today),
                        value = uiState.eggsToday.toString(),
                        icon = Icons.Default.Egg,
                        iconTint = HarvestAmberTertiary,
                        iconBackground = HarvestAmberTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.stat_eggs_7_days),
                        value = uiState.eggsLast7Days.toString(),
                        icon = Icons.Default.Egg,
                        iconTint = HarvestAmberTertiary,
                        iconBackground = HarvestAmberTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.stat_eggs_30_days),
                        value = uiState.eggsLast30Days.toString(),
                        icon = Icons.Default.Egg,
                        iconTint = HarvestAmberTertiary,
                        iconBackground = HarvestAmberTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.stat_avg_daily_eggs),
                        value = String.format(java.util.Locale.US, "%.1f", uiState.avgDailyEggProduction),
                        icon = Icons.Default.TrendingUp,
                        iconTint = ForestGreenPrimary,
                        iconBackground = ForestGreenPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Chart: Daily Egg Production
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.chart_eggs_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        SimpleBarChart(
                            points = uiState.eggChartPoints,
                            barColor = HarvestAmberTertiary,
                            emptyMessage = stringResource(R.string.chart_no_data)
                        )
                    }
                }
            }

            // Key Metrics Section: Mortality & Health
            item {
                Text(
                    text = "Flock Mortality & Health",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.stat_total_adult_deaths),
                        value = uiState.totalAdultDeaths.toString(),
                        icon = Icons.Default.Warning,
                        iconTint = MaterialTheme.colorScheme.error,
                        iconBackground = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.stat_total_chick_deaths),
                        value = uiState.totalChickDeaths.toString(),
                        icon = Icons.Default.Warning,
                        iconTint = MaterialTheme.colorScheme.error,
                        iconBackground = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                val mortalityDisplay = uiState.periodMortalityRatePercent?.let {
                    String.format(java.util.Locale.US, "%.2f%%", it)
                } ?: "N/A"

                StatCard(
                    title = stringResource(R.string.stat_daily_mortality_rate),
                    value = mortalityDisplay,
                    subtitle = stringResource(R.string.stat_mortality_formula_hint),
                    icon = Icons.Default.Info,
                    iconTint = MaterialTheme.colorScheme.error,
                    iconBackground = MaterialTheme.colorScheme.errorContainer,
                    testTag = "stat_mortality_rate"
                )
            }

            // Chart: Daily Deaths
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.chart_deaths_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        TrendLineChart(
                            points = uiState.deathChartPoints,
                            lineColor = MaterialTheme.colorScheme.error,
                            secondaryLineColor = HarvestAmberTertiary,
                            primaryLegendLabel = "Adult Deaths",
                            secondaryLegendLabel = "Chick Deaths",
                            emptyMessage = stringResource(R.string.chart_no_data)
                        )
                    }
                }
            }

            // Current Flock Inventory Status
            item {
                Text(
                    text = "Current Flock Inventory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.stat_current_adults),
                        value = uiState.currentAdults.toString(),
                        icon = Icons.Default.Egg,
                        iconTint = ForestGreenPrimary,
                        iconBackground = ForestGreenPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.stat_current_chicks),
                        value = uiState.currentChicks.toString(),
                        icon = Icons.Default.Egg,
                        iconTint = SproutSecondary,
                        iconBackground = SproutSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                StatCard(
                    title = stringResource(R.string.stat_current_total),
                    value = uiState.currentTotalLiveBirds.toString(),
                    icon = Icons.Default.Egg,
                    iconTint = ForestGreenPrimary,
                    iconBackground = ForestGreenPrimaryContainer
                )
            }

            // Chart: Total Live Bird Population Over Time
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.chart_population_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        TrendLineChart(
                            points = uiState.populationChartPoints,
                            lineColor = ForestGreenPrimary,
                            secondaryLineColor = SproutSecondary,
                            primaryLegendLabel = "Adult Chickens",
                            secondaryLegendLabel = "Chicks",
                            emptyMessage = stringResource(R.string.chart_no_data)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    if (showCustomStartPicker) {
        FarmDatePickerDialog(
            initialDateMillis = DateUtils.parseDateStringToMillis(uiState.customStartDate),
            onDateSelected = { start ->
                showCustomStartPicker = false
                viewModel.setCustomDateRange(start, uiState.customEndDate)
                showCustomEndPicker = true
            },
            onDismiss = { showCustomStartPicker = false }
        )
    }

    if (showCustomEndPicker) {
        FarmDatePickerDialog(
            initialDateMillis = DateUtils.parseDateStringToMillis(uiState.customEndDate),
            onDateSelected = { end ->
                showCustomEndPicker = false
                viewModel.setCustomDateRange(uiState.customStartDate, end)
            },
            onDismiss = { showCustomEndPicker = false }
        )
    }
}
