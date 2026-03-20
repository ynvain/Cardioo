package com.cardioo.domain.usecase

import com.cardioo.domain.repository.MeasurementRepository
import javax.inject.Inject

class DeleteMeasurement @Inject constructor(
    private val repo: MeasurementRepository,
) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}

