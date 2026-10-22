package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.FarmDatePickerDialog
import com.example.util.DateUtils

@Composable
fun SetupScreen(
    onSetupComplete: (name: String, adults: Int, chicks: Int, date: String, lang: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var farmName by remember { mutableStateOf("") }
    var initialAdults by remember { mutableStateOf("0") }
    var initialChicks by remember { mutableStateOf("0") }
    var inventoryDate by remember { mutableStateOf(DateUtils.getCurrentDateString()) }
    var selectedLanguage by remember { mutableStateOf("en") } // "en" or "hi"
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Illustration / Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.img_farm_banner),
                    contentDescription = "Farm Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.setup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Language Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.setup_preferred_language),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedLanguage == "en",
                            onClick = { selectedLanguage = "en" },
                            label = { Text("English") },
                            leadingIcon = if (selectedLanguage == "en") {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.weight(1f).testTag("chip_lang_en")
                        )
                        Spacer(Modifier.width(12.dp))
                        FilterChip(
                            selected = selectedLanguage == "hi",
                            onClick = { selectedLanguage = "hi" },
                            label = { Text("हिंदी (Hindi)") },
                            leadingIcon = if (selectedLanguage == "hi") {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.weight(1f).testTag("chip_lang_hi")
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Farm Name
                    OutlinedTextField(
                        value = farmName,
                        onValueChange = {
                            farmName = it
                            errorMessage = null
                        },
                        label = { Text(stringResource(R.string.setup_farm_name)) },
                        placeholder = { Text(stringResource(R.string.setup_farm_name_hint)) },
                        leadingIcon = {
                            Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_farm_name")
                    )

                    Spacer(Modifier.height(16.dp))

                    // Date Selection
                    OutlinedTextField(
                        value = DateUtils.formatDateForDisplay(inventoryDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.setup_inventory_date)) },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_inventory_date")
                    )

                    Spacer(Modifier.height(16.dp))

                    // Adult Chickens
                    OutlinedTextField(
                        value = initialAdults,
                        onValueChange = { input ->
                            initialAdults = input.filter { it.isDigit() }
                            errorMessage = null
                        },
                        label = { Text(stringResource(R.string.setup_initial_adults)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_initial_adults")
                    )

                    Spacer(Modifier.height(16.dp))

                    // Chicks
                    OutlinedTextField(
                        value = initialChicks,
                        onValueChange = { input ->
                            initialChicks = input.filter { it.isDigit() }
                            errorMessage = null
                        },
                        label = { Text(stringResource(R.string.setup_initial_chicks)) },
                        supportingText = { Text(stringResource(R.string.setup_zero_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_initial_chicks")
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val nameTrimmed = farmName.trim()
                    if (nameTrimmed.isEmpty()) {
                        errorMessage = "Please enter your farm name."
                        return@Button
                    }
                    val adults = initialAdults.toIntOrNull() ?: 0
                    val chicks = initialChicks.toIntOrNull() ?: 0
                    if (adults < 0 || chicks < 0) {
                        errorMessage = "Quantities cannot be negative."
                        return@Button
                    }

                    onSetupComplete(nameTrimmed, adults, chicks, inventoryDate, selectedLanguage)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_setup_save"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.setup_submit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showDatePicker) {
        FarmDatePickerDialog(
            initialDateMillis = DateUtils.parseDateStringToMillis(inventoryDate),
            onDateSelected = { selected ->
                inventoryDate = selected
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
