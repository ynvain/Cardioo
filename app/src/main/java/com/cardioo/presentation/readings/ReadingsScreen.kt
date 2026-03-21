package com.cardioo.presentation.readings

import android.os.Build
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.bpCategory
import com.cardioo.domain.model.displayName
import com.cardioo.presentation.theme.PinkContainer
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
private fun MeasurementCard(
    measurement: HealthMeasurement,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }

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

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${measurement.systolic}/${measurement.diastolic} mmHg", style = MaterialTheme.typography.bodyMedium)
                Text("${measurement.pulse} bpm", style = MaterialTheme.typography.bodyMedium)
                Text("${measurement.weight} ${measurement.weightUnit.displayName()}", style = MaterialTheme.typography.bodyMedium)
            }
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

