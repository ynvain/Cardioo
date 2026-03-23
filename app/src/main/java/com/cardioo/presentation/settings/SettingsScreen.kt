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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.R
import com.cardioo.domain.model.Gender
import com.cardioo.presentation.util.heightUnitString
import com.cardioo.presentation.util.localizeGender
import com.cardioo.presentation.util.weightUnitString
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
    val optional = stringResource(R.string.label_optional)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings_profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
            Text(stringResource(R.string.settings_section_profile), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.name,
                onValueChange = vm::setName,
                label = { Text(stringResource(R.string.label_account_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.heightText,
                onValueChange = vm::setHeightText,
                label = { Text(stringResource(R.string.label_height_unit, heightUnitString(state.heightUnit))) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = vm::toggleHeightUnit) {
                    Text(stringResource(R.string.label_height_toggle, heightUnitString(state.heightUnit)))
                }
                OutlinedButton(onClick = vm::toggleWeightUnit) {
                    Text(stringResource(R.string.label_weight_toggle, weightUnitString(state.weightUnit)))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { showDobPicker = true }) {
                    Text(
                        stringResource(
                            R.string.label_dob_format,
                            state.dob?.toString() ?: optional,
                        ),
                    )
                }
                OutlinedButton(onClick = { vm.setGender(null) }) {
                    Text(
                        stringResource(
                            R.string.label_gender_format,
                            state.gender?.let { localizeGender(it) } ?: optional,
                        ),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Gender.Male, Gender.Female, Gender.Other).forEach { g ->
                    OutlinedButton(onClick = { vm.setGender(if (state.gender == g) null else g) }) {
                        Text(
                            if (state.gender == g) {
                                localizeGender(g) + stringResource(R.string.check_mark_suffix)
                            } else {
                                localizeGender(g)
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.settings_section_reminders), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_reminders_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { vm.save(onBack) },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(if (state.saving) R.string.state_saving else R.string.action_save))
            }
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
                ) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDobPicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}
