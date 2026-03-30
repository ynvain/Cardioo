package com.cardioo.presentation.readings

import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.R
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.bpCategory
import com.cardioo.presentation.util.categoryColor
import com.cardioo.presentation.util.formatLocalizedDateWithoutYear
import com.cardioo.presentation.util.formatLocalizedDayOfWeek
import com.cardioo.presentation.util.formatLocalizedTime
import com.cardioo.presentation.util.getYear
import java.time.ZonedDateTime

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

    // Infinite scroll: `snapshotFlow` turns LazyList scroll state into a cold Flow. We collect it
    // inside `LaunchedEffect` so work runs in a coroutine (collect is suspending) and restarts when
    // keys like list size change, so thresholds stay correct after loads/refreshes.
    LaunchedEffect(
        listState,
        state.totalCount,
        state.measurements.size,
        state.isLoadingMore,
        state.isRefreshing
    ) {
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
            contentPadding = PaddingValues(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            val selectionActive = state.selectedIds.isNotEmpty()
            items(state.measurements, key = { it.id }) { m ->
                MeasurementCard(
                    measurement = m,
                    selected = m.id in state.selectedIds,
                    selectionActive = selectionActive,
                    onEdit = { onEdit(m.id) },
                    onToggleSelect = { vm.toggleSelection(m.id) },
                    onLongPressSelect = { vm.addToSelection(m.id) },
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
    selected: Boolean,
    selectionActive: Boolean,
    onEdit: () -> Unit,
    onToggleSelect: () -> Unit,
    onLongPressSelect: () -> Unit,
) {
    val category = bpCategory(measurement.systolic, measurement.diastolic)
    val currentYear = ZonedDateTime.now().year
    val shape = RoundedCornerShape(12.dp)

    Card(
        modifier = Modifier
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                } else {
                    Modifier
                },
            )
            .combinedClickable(
                onClick = {
                    if (selectionActive) onToggleSelect() else onEdit()
                },
                onLongClick = onLongPressSelect,
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .padding(start = 5.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)
                    .weight(20F),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    formatLocalizedDayOfWeek(measurement.timestampEpochMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatLocalizedDateWithoutYear(measurement.timestampEpochMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val year = getYear(measurement.timestampEpochMillis);
                if (year != currentYear) {
                    Text(
                        year.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
            Column(
                modifier = Modifier
                    .padding(start = 15.dp, top = 10.dp, end = 10.dp, bottom = 5.dp)
                    .weight(80F),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp, end = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(37F),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            stringResource(
                                R.string.format_bp_mmhg,
                                measurement.systolic,
                                measurement.diastolic
                            ),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(28F),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            measurement.pulse?.toString() ?: stringResource(R.string.value_empty),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(30F),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            measurement.weight?.toString() ?: stringResource(R.string.value_empty),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 5.dp)
                ) {
                    measurement.notes?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
