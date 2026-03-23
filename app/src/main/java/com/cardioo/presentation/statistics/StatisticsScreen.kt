package com.cardioo.presentation.statistics

import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.R
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.presentation.util.formatLocalizedDateTime
import com.cardioo.presentation.util.weightUnitString
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    contentPadding: PaddingValues,
    vm: StatisticsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val ok = exportCsv(context, uri, state.measurements)
                snack.showSnackbar(
                    if (ok) context.getString(R.string.export_csv_success)
                    else context.getString(R.string.export_csv_failed),
                )
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.statistics_card_title), style = MaterialTheme.typography.titleMedium)
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                    IconButton(onClick = { exportLauncher.launch(context.getString(R.string.csv_default_filename)) }) {
                        Icon(Icons.Filled.UploadFile, contentDescription = stringResource(R.string.cd_export_csv))
                    }
                }

                val latest = state.summary.latest
                if (latest == null) {
                    Text(stringResource(R.string.statistics_no_readings), style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        formatLocalizedDateTime(latest.timestampEpochMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.format_bp_mmhg, latest.systolic, latest.diastolic),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        listOfNotNull(
                            latest.pulse?.let { stringResource(R.string.format_pulse_bpm, it) },
                            latest.weight?.let {
                                stringResource(
                                    R.string.format_weight_with_unit,
                                    it.toString(),
                                    weightUnitString(latest.weightUnit),
                                )
                            },
                        ).joinToString(stringResource(R.string.bullet_separator)).ifEmpty { stringResource(R.string.value_empty) },
                    )
                }

                if (state.summary.avgSystolic != null && state.summary.avgDiastolic != null) {
                    Text(
                        stringResource(
                            R.string.statistics_avg_bp_format,
                            state.summary.avgSystolic!!,
                            state.summary.avgDiastolic!!,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        pluralStringResource(
                            R.plurals.entries_count,
                            state.summary.count,
                            state.summary.count,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        SnackbarHost(hostState = snack)
    }
}

private suspend fun exportCsv(
    context: Context,
    uri: android.net.Uri,
    measurements: List<HealthMeasurement>,
): Boolean {
    return try {
        context.contentResolver.openOutputStream(uri)?.use { os ->
            val header = context.getString(R.string.csv_header)
            os.write(header.toByteArray())
            measurements.sortedBy { it.timestampEpochMillis }.forEach { m ->
                val notesEscaped = (m.notes ?: "").replace("\"", "\"\"")
                val row =
                    "${m.timestampEpochMillis},${m.systolic},${m.diastolic},${m.pulse ?: ""},${m.weight ?: ""},${m.weightUnit.name},\"$notesEscaped\"\n"
                os.write(row.toByteArray())
            }
        }
        true
    } catch (_: Throwable) {
        false
    }
}
