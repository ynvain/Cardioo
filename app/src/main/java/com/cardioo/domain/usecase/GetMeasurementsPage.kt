package com.cardioo.domain.usecase

import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.repository.MeasurementRepository
import javax.inject.Inject

class GetMeasurementsPage @Inject constructor(
    private val repo: MeasurementRepository,
) {
    /**
     * Load a page for the readings list using cursor pagination.
     *
     * Cursor should be taken from the last item currently loaded.
     */
    suspend operator fun invoke(
        limit: Int,
        beforeTimestampEpochMillis: Long? = null,
        beforeId: Long? = null,
    ): List<HealthMeasurement> =
        repo.getPage(
            limit = limit,
            beforeTimestampEpochMillis = beforeTimestampEpochMillis,
            beforeId = beforeId,
        )
}

