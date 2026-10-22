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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.FarmDatePickerDialog
import com.example.ui.components.FarmTimePickerDialog
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ForestGreenPrimaryContainer
import com.example.ui.theme.HarvestAmberTertiary
import com.example.ui.theme.HarvestAmberTertiaryContainer
import com.example.ui.theme.SproutSecondary
import com.example.ui.theme.SproutSecondaryContainer
import com.example.ui.viewmodel.DailyRecordFormViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFormScreen(
    viewModel: DailyRecordFormViewModel,
    recordId: Long? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(recordId) {
        viewModel.initialize(recordId)
    }

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            onSaveSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) {
                            stringResource(R.string.record_form_title_edit)
                        } else {
                            stringResource(R.string.record_form_title_add)
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // Existing record overwrite notification
            if (uiState.isDateOverwriting && !uiState.isEditMode) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.msg_date_exists_overwrite, uiState.recordDateString),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Section 1: Date & Time Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.record_date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Date field
                            OutlinedTextField(
                                value = DateUtils.formatDateForDisplay(uiState.recordDateString),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.record_date)) },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ForestGreenPrimary)
                                },
                                modifier = Modifier.weight(1f).testTag("input_record_date")
                            )

                            Button(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.align(Alignment.CenterVertically).testTag("btn_pick_date")
                            ) {
                                Text(stringResource(R.string.record_select_date))
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Time field
                            OutlinedTextField(
                                value = DateUtils.formatTimeForDisplay(uiState.recordTimeMillis),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.record_time)) },
                                leadingIcon = {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = ForestGreenPrimary)
                                },
                                modifier = Modifier.weight(1f).testTag("input_record_time")
                            )

                            Button(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.align(Alignment.CenterVertically).testTag("btn_pick_time")
                            ) {
                                Text(stringResource(R.string.record_select_time))
                            }
                        }
                    }
                }
            }

            // Section 2: Adult Chickens
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_adult_chickens),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )

                        Spacer(Modifier.height(12.dp))

                        // Opening count banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ForestGreenPrimaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.label_opening_adults),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${uiState.openingAdults}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Added
                        OutlinedTextField(
                            value = uiState.adultsAdded,
                            onValueChange = { viewModel.onAdultsAddedChanged(it) },
                            label = { Text(stringResource(R.string.label_adults_added)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_adults_added")
                        )

                        Spacer(Modifier.height(10.dp))

                        // Deaths
                        OutlinedTextField(
                            value = uiState.adultDeaths,
                            onValueChange = { viewModel.onAdultDeathsChanged(it) },
                            label = { Text(stringResource(R.string.label_adult_deaths)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_adult_deaths")
                        )

                        Spacer(Modifier.height(10.dp))

                        // Sold
                        OutlinedTextField(
                            value = uiState.adultsSold,
                            onValueChange = { viewModel.onAdultsSoldChanged(it) },
                            label = { Text(stringResource(R.string.label_adults_sold)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_adults_sold")
                        )

                        Spacer(Modifier.height(14.dp))

                        // Calculated Closing Adults Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (uiState.isAdultNegative) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.label_closing_adults),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "(${uiState.openingAdults} + ${uiState.intAdultsAdded} - ${uiState.intAdultDeaths} - ${uiState.intAdultsSold})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${uiState.calculatedClosingAdults}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isAdultNegative) MaterialTheme.colorScheme.error else ForestGreenPrimary
                            )
                        }

                        if (uiState.isAdultNegative) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.msg_error_negative_adults, uiState.calculatedClosingAdults),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Section 3: Chicks
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_chicks),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SproutSecondary
                        )

                        Spacer(Modifier.height(12.dp))

                        // Opening chicks banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SproutSecondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.label_opening_chicks),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${uiState.openingChicks}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SproutSecondary
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Added
                        OutlinedTextField(
                            value = uiState.chicksAdded,
                            onValueChange = { viewModel.onChicksAddedChanged(it) },
                            label = { Text(stringResource(R.string.label_chicks_added)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_chicks_added")
                        )

                        Spacer(Modifier.height(10.dp))

                        // Deaths
                        OutlinedTextField(
                            value = uiState.chickDeaths,
                            onValueChange = { viewModel.onChickDeathsChanged(it) },
                            label = { Text(stringResource(R.string.label_chick_deaths)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_chick_deaths")
                        )

                        Spacer(Modifier.height(10.dp))

                        // Sold
                        OutlinedTextField(
                            value = uiState.chicksSold,
                            onValueChange = { viewModel.onChicksSoldChanged(it) },
                            label = { Text(stringResource(R.string.label_chicks_sold)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_chicks_sold")
                        )

                        Spacer(Modifier.height(14.dp))

                        // Calculated Closing Chicks Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (uiState.isChickNegative) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.label_closing_chicks),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "(${uiState.openingChicks} + ${uiState.intChicksAdded} - ${uiState.intChickDeaths} - ${uiState.intChicksSold})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${uiState.calculatedClosingChicks}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isChickNegative) MaterialTheme.colorScheme.error else SproutSecondary
                            )
                        }

                        if (uiState.isChickNegative) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.msg_error_negative_chicks, uiState.calculatedClosingChicks),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Section 4: Egg Production
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_egg_production),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HarvestAmberTertiary
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.eggsCollected,
                            onValueChange = { viewModel.onEggsCollectedChanged(it) },
                            label = { Text(stringResource(R.string.label_eggs_collected)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_eggs_collected")
                        )
                    }
                }
            }

            // Section 5: Live Summary Totals
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ForestGreenPrimaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_total_live),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${uiState.totalLiveBirds}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_total_deaths_today),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${uiState.totalDeathsToday}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Section 6: Additional Observations & Treatments
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_notes_health),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.onNotesChanged(it) },
                            label = { Text(stringResource(R.string.label_notes)) },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("input_notes")
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.healthObservations,
                            onValueChange = { viewModel.onHealthObservationsChanged(it) },
                            label = { Text(stringResource(R.string.label_health_obs)) },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("input_health_obs")
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.treatments,
                            onValueChange = { viewModel.onTreatmentsChanged(it) },
                            label = { Text(stringResource(R.string.label_treatments)) },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("input_treatments")
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f).height(50.dp).testTag("btn_cancel_record")
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }

                    Button(
                        onClick = { viewModel.saveRecord(onSaveSuccess) },
                        enabled = uiState.canSave,
                        modifier = Modifier.weight(1f).height(50.dp).testTag("btn_save_record"),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_save_record),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(30.dp))
            }
        }
    }

    if (showDatePicker) {
        FarmDatePickerDialog(
            initialDateMillis = DateUtils.parseDateStringToMillis(uiState.recordDateString),
            onDateSelected = { selected ->
                viewModel.onDateChanged(selected)
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        FarmTimePickerDialog(
            initialTimeMillis = uiState.recordTimeMillis,
            onTimeSelected = { selected ->
                viewModel.onTimeChanged(selected)
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
