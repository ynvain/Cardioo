package com.cardioo.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.domain.model.Gender
import com.cardioo.domain.model.displayName
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    var showDobPicker by remember { mutableStateOf(false) }
    val datePickerState = androidx.compose.material3.rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Profile", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::setName,
                label = { Text("Account name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.heightText,
                onValueChange = vm::setHeightText,
                label = { Text("Height (${state.heightUnit.displayName()})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = vm::toggleHeightUnit) { Text("Height: ${state.heightUnit.displayName()}") }
                OutlinedButton(onClick = vm::toggleWeightUnit) { Text("Weight: ${state.weightUnit.displayName()}") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { showDobPicker = true }) {
                    Text("DOB: ${state.dob?.toString() ?: "Optional"}")
                }
                OutlinedButton(onClick = { vm.setGender(null) }) {
                    Text("Gender: ${state.gender?.name ?: "Optional"}")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Gender.Male, Gender.Female, Gender.Other).forEach { g ->
                    OutlinedButton(onClick = { vm.setGender(if (state.gender == g) null else g) }) {
                        Text(if (state.gender == g) "${g.name} ✓" else g.name)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("Reminders", style = MaterialTheme.typography.titleMedium)
            Text(
                "Optional reminders can be added later without extra permissions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { vm.save(onBack) },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (state.saving) "Saving..." else "Save") }
        }
    }

    if (showDobPicker) {
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDobPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        val date =
                            millis?.let {
                                Instant.fromEpochMilliseconds(it)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date
                            }
                        vm.setDob(date)
                        showDobPicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDobPicker = false }) { Text("Cancel") }
            },
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}

