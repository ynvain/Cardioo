package com.cardioo.presentation.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.UserProfile
import com.cardioo.domain.usecase.DeleteMeasurement
import com.cardioo.domain.usecase.GetMeasurementsPage
import com.cardioo.domain.usecase.ObserveMeasurementCount
import com.cardioo.domain.usecase.ObserveMeasurements
import com.cardioo.domain.usecase.ObserveProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    observeMeasurementCount: ObserveMeasurementCount,
    observeMeasurements: ObserveMeasurements,
    observeProfile: ObserveProfile,
    private val getMeasurementsPage: GetMeasurementsPage,
    private val deleteMeasurement: DeleteMeasurement,
) : ViewModel() {
    /**
     * Readings pagination:
     * - pageSize is fixed (30).
     * - cursor is (timestampEpochMillis, id) of the last item currently loaded.
     *
     * We avoid OFFSET because inserting a new measurement at the top shifts offsets and can
     * re-load items that are already in memory (duplicates). Duplicates then crash LazyColumn
     * because keys must be unique.
     *
     * **Updates:** total count often stays the same when a row is edited, so we also observe
     * [ObserveMeasurements] and merge fresh rows by id into the already-loaded list.
     */
    private val pageSize = 30
    private val measurements = MutableStateFlow<List<HealthMeasurement>>(emptyList())
    private val totalCount = MutableStateFlow(0)
    private val refreshing = MutableStateFlow(false)
    private val loadingMore = MutableStateFlow(false)
    val newAdded = MutableStateFlow(false)
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private var activeAccountId: Long? = null

    val state: StateFlow<State> =
        combine(
            measurements,
            observeProfile(),
            totalCount,
            refreshing,
            loadingMore,
            selectedIds,
        ) { args: Array<Any?> ->
            val m = args[0] as List<HealthMeasurement>
            val profile = args[1] as UserProfile?
            val total = args[2] as Int
            val isRefreshing = args[3] as Boolean
            val isLoadingMore = args[4] as Boolean
            val selected = args[5] as Set<Long>

            State(
                measurements = m,
                profile = profile,
                isRefreshing = isRefreshing,
                isLoadingMore = isLoadingMore,
                totalCount = total,
                selectedIds = selected,
            )
        }.stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
            State()
        )

    data class State(
        val measurements: List<HealthMeasurement> = emptyList(),
        val profile: UserProfile? = null,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val totalCount: Int = 0,
        val selectedIds: Set<Long> = emptySet(),
    )


    init {
        viewModelScope.launch {
            observeProfile()
                .map { it?.id }
                .distinctUntilChanged()
                .collect { accountId ->
                    // On account switch we reset list state before loading data for the new account.
                    activeAccountId = accountId
                    measurements.value = emptyList()
                    totalCount.value = 0
                    selectedIds.value = emptySet()
                    loadFirstPage()
                }
        }

        viewModelScope.launch {
            observeMeasurementCount()
                .distinctUntilChanged()
                .collect { c ->
                    val prev = totalCount.value
                    totalCount.value = c
                    if (measurements.value.isEmpty()) return@collect
                    // Keep only current account rows and refresh safely as DB changes.
                    if (c < measurements.value.size) {
                        loadFirstPage()
                    } else if (c > prev) {
                        prependNewestForCurrentAccount()
                    }
                }
        }

        // When any measurement row changes (including edits where COUNT is unchanged), Room emits
        // a new list; we refresh only the items already shown in the paginated list.
        viewModelScope.launch {
            observeMeasurements().collect { all ->
                val accountId = activeAccountId ?: return@collect
                measurements.update { cur ->
                    if (cur.isEmpty()) return@update cur
                    if (cur.any { it.userId != accountId }) return@update cur
                    val byId = all.associateBy { it.id }
                    cur.mapNotNull { m -> byId[m.id] }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.update { true }
            loadFirstPage()
            delay(450) // local DB, just UI affordance
            refreshing.update { false }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            if (loadingMore.value || refreshing.value) return@launch
            val current = measurements.value
            if (current.size >= totalCount.value) return@launch
            loadingMore.value = true
            try {
                val last = current.lastOrNull()
                val next =
                    if (last == null) {
                        getMeasurementsPage(limit = pageSize)
                    } else {
                        getMeasurementsPage(
                            limit = pageSize,
                            beforeTimestampEpochMillis = last.timestampEpochMillis,
                            beforeId = last.id,
                        )
                    }
                if (next.isNotEmpty()) {
                    // Defensive dedupe: even with a stable cursor, never allow duplicate ids into UI.
                    measurements.value = (current + next).distinctBy { it.id }
                }
            } finally {
                loadingMore.value = false
            }
        }
    }

    fun toggleSelection(id: Long) {
        selectedIds.update { cur -> if (id in cur) cur - id else cur + id }
    }

    /** Long-press: enter multi-select and include this row (does not toggle off). */
    fun addToSelection(id: Long) {
        selectedIds.update { it + id }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        val ids = selectedIds.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { deleteMeasurement(it) }
            measurements.update { it.filter { m -> m.id !in ids } }
            clearSelection()
            if (measurements.value.size < totalCount.value) {
                loadNextPage()
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            deleteMeasurement(id)
            // Optimistic UI: remove immediately. Count observer will reconcile.
            measurements.update { it.filterNot { m -> m.id == id } }
            selectedIds.update { it - id }
            if (measurements.value.size < totalCount.value) {
                // Fill gap after deletion if more exist.
                loadNextPage()
            }
        }
    }

    private suspend fun loadFirstPage() {
        loadingMore.value = true
        try {
            val first = getMeasurementsPage(limit = pageSize)
            measurements.value = first
        } finally {
            loadingMore.value = false
        }
    }

    // Pull newest rows for current account and prepend them, deduped by id.
    private suspend fun prependNewestForCurrentAccount() {
        if (activeAccountId == null) return
        if (refreshing.value) return
        loadingMore.value = true
        newAdded.value = true;
        try {
            val first = getMeasurementsPage(limit = pageSize)
            val merged = (first + measurements.value).distinctBy { it.id }
            measurements.value = merged
        } finally {
            loadingMore.value = false
        }
    }

    fun resetAddedNewFlag() {
        newAdded.value = false
    }
}
