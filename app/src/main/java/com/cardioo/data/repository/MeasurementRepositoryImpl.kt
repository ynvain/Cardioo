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

    override fun observeCount(): Flow<Int> =
        session.currentAccountId.flatMapLatest { userId ->
            if (userId == null) flowOf(0) else dao.observeCountForUser(userId)
        }

    override suspend fun getPage(
        limit: Int,
        beforeTimestampEpochMillis: Long?,
        beforeId: Long?,
    ): List<HealthMeasurement> {
        val userId = session.currentAccountId.first() ?: return emptyList()
        val entities =
            if (beforeTimestampEpochMillis == null || beforeId == null) {
                dao.getFirstPageForUser(userId, limit)
            } else {
                dao.getNextPageForUser(
                    userId = userId,
                    beforeTimestamp = beforeTimestampEpochMillis,
                    beforeId = beforeId,
                    limit = limit,
                )
            }
        return entities.map { it.toDomain() }
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

