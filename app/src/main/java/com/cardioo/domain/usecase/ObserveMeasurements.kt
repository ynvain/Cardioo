package com.cardioo.domain.usecase

import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMeasurements @Inject constructor(
    private val repo: MeasurementRepository,
) {
    operator fun invoke(): Flow<List<HealthMeasurement>> = repo.observeAll()
}

