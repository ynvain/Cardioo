package com.cardioo.presentation.entry


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.R
import com.cardioo.presentation.util.categoryColor
import com.cardioo.presentation.util.formatLocalizedDate
import com.cardioo.presentation.util.formatLocalizedTime
import com.cardioo.presentation.util.localizeBpCategory
import com.cardioo.presentation.util.weightUnitString
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
    val focusManager = LocalFocusManager.current

    LaunchedEffect(measurementId) {
        vm.load(measurementId)
    }

    LaunchedEffect(state.loading, measurementId) {
        if (!state.loading && measurementId == null) focusSystolic.requestFocus()
    }

    val datePart =
        remember(state.timestampEpochMillis) { formatLocalizedDate(state.timestampEpochMillis) }
    val timePart =
        remember(state.timestampEpochMillis) { formatLocalizedTime(state.timestampEpochMillis) }
    val bpCategory = vm.computedBpCategory()
    val bmi = vm.computedBmi()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (measurementId == null) R.string.title_add_reading else R.string.title_edit_reading,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
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
                        val cal = Calendar.getInstance()
                            .apply { timeInMillis = state.timestampEpochMillis }
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
                ) { Text(datePart) }

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                            .apply { timeInMillis = state.timestampEpochMillis }
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
                ) { Text(timePart) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.systolicText,
                    onValueChange = { new ->
                        vm.setSystolicText(new)
                        new.toIntOrNull()?.let { v ->
                            if (v in 50..250) focusDiastolic.requestFocus()
                        }
                    },
                    label = { Text(stringResource(R.string.label_systolic)) },
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
                            if (v in 30..150) focusPulse.requestFocus()
                        }
                    },
                    label = { Text(stringResource(R.string.label_diastolic)) },
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
                            if (v in 40..200) focusManager.clearFocus()
                        }
                    }
                },
                label = { Text(stringResource(R.string.label_pulse_optional)) },
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
                            focusManager.clearFocus()
                        }
                    },
                    label = {
                        Text(
                            stringResource(
                                R.string.label_weight_optional,
                                weightUnitString(state.weightUnit),
                            ),
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusWeight),
                    singleLine = true,
                )
                IconButton(onClick = vm::toggleWeightUnit) {
                    Icon(
                        Icons.Filled.SwapHoriz,
                        contentDescription = stringResource(R.string.cd_toggle_weight_unit),
                    )
                }
            }

            bpCategory?.let { cat ->
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        stringResource((R.string.bp_category_format)),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        localizeBpCategory(cat),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(categoryColor(cat))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFFFFF),
                    )
                }
            }
            bmi?.let { value ->
                Text(
                    stringResource(R.string.bmi_format, value),
                    style = MaterialTheme.typography.bodyMedium
                )
            } ?: run {
                if (state.profile == null) {
                    Text(
                        stringResource(R.string.bmi_set_height_hint),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = vm::setNotes,
                label = { Text(stringResource(R.string.label_notes_optional)) },
                modifier = Modifier
                    .fillMaxWidth(),
                minLines = 3,
            )

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { vm.save(onDone) },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(if (state.saving) R.string.state_saving else R.string.action_save))
            }
        }
    }
}
