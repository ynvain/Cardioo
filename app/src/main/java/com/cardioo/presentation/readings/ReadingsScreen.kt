package com.cardioo.presentation.readings

import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.bpCategory
import com.cardioo.domain.model.displayName
import com.cardioo.presentation.theme.PinkContainer
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReadingsScreen(
    contentPadding: PaddingValues,
    onEdit: (Long) -> Unit,
    vm: ReadingsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                // Export is intentionally kept UI-driven (system picker) so no storage permissions
                // are required and the user controls the destination.
                val ok = exportCsv(context, uri, state.measurements)
                snack.showSnackbar(if (ok) "Exported CSV" else "Export failed")
            }
        }

    val pullState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = vm::refresh,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .pullRefresh(pullState),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DashboardCard(
                    summary = state.summary,
                    onExport = { exportLauncher.launch("cardioo_readings.csv") },
                )
            }

            items(state.measurements, key = { it.id }) { m ->
                MeasurementCard(
                    measurement = m,
                    onEdit = { onEdit(m.id) },
                    onDelete = { vm.delete(m.id) },
                )
            }

            item { Spacer(Modifier.size(72.dp)) }
        }

        PullRefreshIndicator(
            refreshing = state.isRefreshing,
            state = pullState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colorScheme.primary,
        )

        SnackbarHost(
            hostState = snack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DashboardCard(
    summary: ReadingsViewModel.Summary,
    onExport: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onExport) {
                    Icon(Icons.Filled.UploadFile, contentDescription = "Export CSV")
                }
            }

            val latest = summary.latest
            if (latest == null) {
                Text("No readings yet. Tap the pink + button to add one.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    "Latest: ${formatDateTime(latest.timestampEpochMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("BP: ${latest.systolic}/${latest.diastolic} mmHg", style = MaterialTheme.typography.bodyLarge)
                Text("Pulse: ${latest.pulse} bpm • Weight: ${latest.weight} ${latest.weightUnit.displayName()}")
            }

            if (summary.avgSystolic != null && summary.avgDiastolic != null) {
                Text(
                    "Average BP: ${summary.avgSystolic}/${summary.avgDiastolic} mmHg • Entries: ${summary.count}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MeasurementCard(
    measurement: HealthMeasurement,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val category = bpCategory(measurement.systolic, measurement.diastolic)

    Card(
        modifier = Modifier.clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatDateTime(measurement.timestampEpochMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { menu = false; onEdit() },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { menu = false; onDelete() },
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("BP: ${measurement.systolic}/${measurement.diastolic} mmHg", style = MaterialTheme.typography.bodyLarge)
                Text(
                    category.label,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(PinkContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8A1F38),
                )
            }

            Text("Pulse: ${measurement.pulse} bpm", style = MaterialTheme.typography.bodyMedium)
            Text("Weight: ${measurement.weight} ${measurement.weightUnit.displayName()}", style = MaterialTheme.typography.bodyMedium)
            measurement.notes?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
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
                // CSV escaping: double quotes are doubled, then the whole field is quoted.
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

