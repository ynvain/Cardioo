package com.cardioo.presentation.entry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.domain.model.displayName
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementEntryScreen(
    measurementId: Long?,
    onDone: () -> Unit,
    vm: MeasurementEntryViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val focusSystolic = remember { FocusRequester() }
    val focusDiastolic = remember { FocusRequester() }
    val focusPulse = remember { FocusRequester() }
    val focusWeight = remember { FocusRequester() }
    val focusNotes = remember { FocusRequester() }

    LaunchedEffect(measurementId) {
        vm.load(measurementId)
    }

    // Only auto-focus systolic on new entry; avoids stealing focus when editing an existing reading.
    LaunchedEffect(state.loading, measurementId) {
        if (!state.loading && measurementId == null) focusSystolic.requestFocus()
    }

    val dtText = rememberFormattedDateTime(state.timestampEpochMillis)
    val bpCategory = vm.computedBpCategory()
    val bmi = vm.computedBmi()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (measurementId == null) "Add reading" else "Edit reading") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = state.timestampEpochMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                cal.set(Calendar.YEAR, year)
                                cal.set(Calendar.MONTH, month)
                                cal.set(Calendar.DAY_OF_MONTH, day)
                                vm.setTimestamp(cal.timeInMillis)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    },
                ) { Text(dtText.substringBefore(" · ")) }

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = state.timestampEpochMillis }
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                cal.set(Calendar.HOUR_OF_DAY, hour)
                                cal.set(Calendar.MINUTE, minute)
                                vm.setTimestamp(cal.timeInMillis)
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            false,
                        ).show()
                    },
                ) { Text(dtText.substringAfter(" · ")) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.systolicText,
                    onValueChange = { new ->
                        vm.setSystolicText(new)
                        new.toIntOrNull()?.let { v ->
                            if (v in 90..180) focusDiastolic.requestFocus()
                        }
                    },
                    label = { Text("Systolic (90-180)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusSystolic),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.diastolicText,
                    onValueChange = { new ->
                        vm.setDiastolicText(new)
                        new.toIntOrNull()?.let { v ->
                            if (v in 60..120) focusPulse.requestFocus()
                        }
                    },
                    label = { Text("Diastolic (60-120)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusDiastolic),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = state.pulseText,
                onValueChange = { new ->
                    vm.setPulseText(new)
                    if (new.isNotEmpty()) {
                        new.toIntOrNull()?.let { v ->
                            if (v in 40..200) focusWeight.requestFocus()
                        }
                    }
                },
                label = { Text("Pulse (optional, 40-200 bpm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusPulse),
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.weightText,
                    onValueChange = { new ->
                        vm.setWeightText(new)
                        if (vm.isWeightTextCompleteForFocus(new)) {
                            focusNotes.requestFocus()
                        }
                    },
                    label = { Text("Weight (optional, ${state.weightUnit.displayName()})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusWeight),
                    singleLine = true,
                )
                IconButton(onClick = vm::toggleWeightUnit) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = "Toggle weight unit")
                }
            }

            bpCategory?.let {
                Text("BP category: $it", style = MaterialTheme.typography.bodyMedium)
            }
            bmi?.let {
                Text("BMI: $it", style = MaterialTheme.typography.bodyMedium)
            } ?: run {
                if (state.profile == null) {
                    Text("BMI: Set your height in Settings.", style = MaterialTheme.typography.bodySmall)
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = vm::setNotes,
                label = { Text("Notes (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusNotes),
                minLines = 3,
            )

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { vm.save(onDone) },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.saving) "Saving..." else "Save")
            }
        }
    }
}

@Composable
private fun rememberFormattedDateTime(epochMillis: Long): String {
    val dt = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy · h:mm a")
    return formatter.format(dt)
}
