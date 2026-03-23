package com.cardioo.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
fun OnboardingScreen(
    onDone: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val optional = stringResource(R.string.label_optional)

    var showDobPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.onboarding_welcome_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.onboarding_subtitle), style = MaterialTheme.typography.bodyMedium)

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
                        state.dateOfBirth?.toString() ?: optional,
                    ),
                )
            }
            GenderChipRow(
                selected = state.gender,
                onSelected = vm::setGender,
                modifier = Modifier.weight(1f),
            )
        }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { vm.save(onDone) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.saving,
        ) { Text(stringResource(if (state.saving) R.string.state_saving else R.string.action_continue)) }
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

@Composable
private fun GenderChipRow(
    selected: Gender?,
    onSelected: (Gender?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Gender.Male, Gender.Female, Gender.Other).forEach { g ->
            val isSelected = selected == g
            OutlinedButton(onClick = { onSelected(if (isSelected) null else g) }) {
                Text(
                    if (isSelected) {
                        localizeGender(g) + stringResource(R.string.check_mark_suffix)
                    } else {
                        localizeGender(g)
                    },
                )
            }
        }
    }
}
