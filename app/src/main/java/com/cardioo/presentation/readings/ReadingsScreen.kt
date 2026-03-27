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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.R
import com.cardioo.domain.model.BpCategory
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.bpCategory
import com.cardioo.presentation.theme.PinkContainer
import com.cardioo.presentation.theme.PinkPrimary
import com.cardioo.presentation.util.categoryColor
import com.cardioo.presentation.util.formatLocalizedDate
import com.cardioo.presentation.util.formatLocalizedDateTime
import com.cardioo.presentation.util.formatLocalizedTime
import com.cardioo.presentation.util.localizeBpCategory
import com.cardioo.presentation.util.weightUnitString

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReadingsScreen(
    contentPadding: PaddingValues,
    onEdit: (Long) -> Unit,
    vm: ReadingsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val hasMore = state.measurements.size < state.totalCount

    val pullState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = vm::refresh,
    )

    LaunchedEffect(listState, state.totalCount, state.measurements.size, state.isLoadingMore, state.isRefreshing) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible
        }.collect { lastVisible ->
            val threshold = 5
            val shouldLoad =
                hasMore &&
                    !state.isLoadingMore &&
                    !state.isRefreshing &&
                    lastVisible >= (state.measurements.size - 1 - threshold).coerceAtLeast(0)
            if (shouldLoad) vm.loadNextPage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .pullRefresh(pullState),
    ) {
        LazyColumn(
            state = listState,
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

            if (state.isLoadingMore && !state.isRefreshing) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
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

@Composable
private fun MeasurementCard(
    measurement: HealthMeasurement,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val category = bpCategory(measurement.systolic, measurement.diastolic)

    Card(
        modifier = Modifier.clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    formatLocalizedDate(measurement.timestampEpochMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            Text(
                formatLocalizedTime(measurement.timestampEpochMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

        }
            Divider(
                color = categoryColor(category),
                modifier = Modifier
                    .height(50.dp)
                    .width(3.5.dp)
            )
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(15.dp)
                        .background(categoryColor(category), CircleShape),
                )

            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    stringResource(R.string.format_bp_mmhg, measurement.systolic, measurement.diastolic),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    measurement.pulse?.toString() ?: stringResource(R.string.value_empty),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    measurement.weight?.toString() ?: stringResource(R.string.value_empty),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            measurement.notes?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
}
