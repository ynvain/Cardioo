package com.cardioo.domain.usecase

import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.repository.MeasurementRepository
import javax.inject.Inject

class GetMeasurement @Inject constructor(
    private val repo: MeasurementRepository,
) {
    suspend operator fun invoke(id: Long): HealthMeasurement? = repo.getById(id)
}

