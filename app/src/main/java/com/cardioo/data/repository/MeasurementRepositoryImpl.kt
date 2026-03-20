package com.cardioo.data.repository

import com.cardioo.data.db.dao.HealthMeasurementDao
import com.cardioo.data.mapper.toDomain
import com.cardioo.data.mapper.toEntity
import com.cardioo.data.session.AccountSessionDataSource
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MeasurementRepositoryImpl @Inject constructor(
    private val dao: HealthMeasurementDao,
    private val session: AccountSessionDataSource,
) : MeasurementRepository {
    override fun observeAll(): Flow<List<HealthMeasurement>> =
        session.currentAccountId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(emptyList())
            } else {
                dao.observeAllForUser(userId).map { list -> list.map { it.toDomain() } }
            }
        }

    override suspend fun getById(id: Long): HealthMeasurement? {
        val userId = session.currentAccountId.first() ?: return null
        return dao.getByIdForUser(id, userId)?.toDomain()
    }

    override suspend fun upsert(measurement: HealthMeasurement): Long {
        val userId = session.currentAccountId.first() ?: return -1L
        return dao.upsert(measurement.copy(userId = userId).toEntity())
    }

    override suspend fun delete(id: Long) {
        val userId = session.currentAccountId.first() ?: return
        dao.deleteByIdForUser(id, userId)
    }
}

