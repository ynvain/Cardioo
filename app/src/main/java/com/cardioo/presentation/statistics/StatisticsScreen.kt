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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.displayName
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    contentPadding: PaddingValues,
    vm: StatisticsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val ok = exportCsv(context, uri, state.measurements)
                snack.showSnackbar(if (ok) "Exported CSV" else "Export failed")
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
                    Text("Statistics", style = MaterialTheme.typography.titleMedium)
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                    IconButton(onClick = { exportLauncher.launch("cardioo_readings.csv") }) {
                        Icon(Icons.Filled.UploadFile, contentDescription = "Export CSV")
                    }
                }

                val latest = state.summary.latest
                if (latest == null) {
                    Text("No readings yet.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        formatDateTime(latest.timestampEpochMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("${latest.systolic}/${latest.diastolic} mmHg", style = MaterialTheme.typography.bodyLarge)
                    Text("${latest.pulse} bpm  •  ${latest.weight} ${latest.weightUnit.displayName()}")
                }

                if (state.summary.avgSystolic != null && state.summary.avgDiastolic != null) {
                    Text(
                        "Average ${state.summary.avgSystolic}/${state.summary.avgDiastolic} mmHg",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text("${state.summary.count} entries", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        SnackbarHost(hostState = snack)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateTime(epochMillis: Long): String {
    val dt = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy · h:mm a")
    return formatter.format(dt)
}

private suspend fun exportCsv(
    context: Context,
    uri: android.net.Uri,
    measurements: List<HealthMeasurement>,
): Boolean {
    return try {
        context.contentResolver.openOutputStream(uri)?.use { os ->
            val header = "timestamp,systolic,diastolic,pulse,weight,weightUnit,notes\n"
            os.write(header.toByteArray())
            measurements.sortedBy { it.timestampEpochMillis }.forEach { m ->
                val notesEscaped = (m.notes ?: "").replace("\"", "\"\"")
                val row =
                    "${m.timestampEpochMillis},${m.systolic},${m.diastolic},${m.pulse},${m.weight},${m.weightUnit.name},\"$notesEscaped\"\n"
                os.write(row.toByteArray())
            }
        }
        true
    } catch (_: Throwable) {
        false
    }
}

