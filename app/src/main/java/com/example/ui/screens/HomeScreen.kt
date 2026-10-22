package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.entity.Farm
import com.example.domain.model.DashboardStats
import com.example.ui.components.StatCard
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ForestGreenPrimaryContainer
import com.example.ui.theme.HarvestAmberTertiary
import com.example.ui.theme.HarvestAmberTertiaryContainer
import com.example.ui.theme.SproutSecondary
import com.example.ui.theme.SproutSecondaryContainer
import com.example.util.DateUtils

@Composable
fun HomeScreen(
    farm: Farm?,
    stats: DashboardStats,
    onNavigateToAddRecord: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddRecord,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.dashboard_btn_add_record)) },
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_record")
            )
        }
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
                // Hero Banner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(R.drawable.img_farm_banner),
                            contentDescription = "Farm Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = farm?.name ?: stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.dashboard_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Last updated info chip
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (stats.lastRecordDateString != null) {
                            "${stringResource(R.string.dashboard_last_record)}: ${DateUtils.formatDateForDisplay(stats.lastRecordDateString)}"
                        } else {
                            stringResource(R.string.dashboard_no_records_yet)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Section: Flock Inventory Overview
            item {
                Text(
                    text = stringResource(R.string.dashboard_flock_status),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.dashboard_live_adults),
                        value = stats.liveAdultChickens.toString(),
                        icon = Icons.Default.Egg,
                        iconTint = ForestGreenPrimary,
                        iconBackground = ForestGreenPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_live_adults"
                    )
                    StatCard(
                        title = stringResource(R.string.dashboard_live_chicks),
                        value = stats.liveChicks.toString(),
                        icon = Icons.Default.Egg,
                        iconTint = SproutSecondary,
                        iconBackground = SproutSecondaryContainer,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_live_chicks"
                    )
                }
            }

            item {
                StatCard(
                    title = stringResource(R.string.dashboard_total_live),
                    value = stats.totalLiveBirds.toString(),
                    icon = Icons.Default.Egg,
                    iconTint = ForestGreenPrimary,
                    iconBackground = ForestGreenPrimaryContainer,
                    testTag = "stat_total_live"
                )
            }

            // Section: Today's Performance
            item {
                Text(
                    text = stringResource(R.string.dashboard_today_performance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                StatCard(
                    title = stringResource(R.string.dashboard_eggs_today),
                    value = stats.eggsToday.toString(),
                    icon = Icons.Default.Egg,
                    iconTint = HarvestAmberTertiary,
                    iconBackground = HarvestAmberTertiaryContainer,
                    testTag = "stat_eggs_today"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.dashboard_adult_deaths_today),
                        value = stats.adultDeathsToday.toString(),
                        icon = Icons.Default.Warning,
                        iconTint = MaterialTheme.colorScheme.error,
                        iconBackground = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_adult_deaths_today"
                    )
                    StatCard(
                        title = stringResource(R.string.dashboard_chick_deaths_today),
                        value = stats.chickDeathsToday.toString(),
                        icon = Icons.Default.Warning,
                        iconTint = MaterialTheme.colorScheme.error,
                        iconBackground = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_chick_deaths_today"
                    )
                }
            }

            item {
                StatCard(
                    title = stringResource(R.string.dashboard_total_deaths_today),
                    value = stats.totalDeathsToday.toString(),
                    icon = Icons.Default.Warning,
                    iconTint = MaterialTheme.colorScheme.error,
                    iconBackground = MaterialTheme.colorScheme.errorContainer,
                    testTag = "stat_total_deaths_today"
                )
            }

            // Section: Quick Action Shortcuts
            item {
                Text(
                    text = stringResource(R.string.dashboard_quick_actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToRecords,
                        modifier = Modifier.weight(1f).testTag("btn_quick_history")
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.dashboard_quick_records))
                    }

                    OutlinedButton(
                        onClick = onNavigateToReports,
                        modifier = Modifier.weight(1f).testTag("btn_quick_reports")
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.dashboard_quick_reports))
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.fillMaxWidth().testTag("btn_quick_backup")
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.dashboard_quick_backup))
                }
            }

            item {
                Spacer(Modifier.height(80.dp)) // Extra space for FAB
            }
        }
    }
}
