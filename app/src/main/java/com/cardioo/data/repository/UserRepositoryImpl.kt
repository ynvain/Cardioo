package com.cardioo.data.repository

import com.cardioo.data.db.dao.UserDao
import com.cardioo.data.mapper.toDomain
import com.cardioo.data.mapper.toEntity
import com.cardioo.data.session.AccountSessionDataSource
import com.cardioo.domain.model.UserProfile
import com.cardioo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao,
    private val session: AccountSessionDataSource,
) : UserRepository {
    override fun observeProfile(): Flow<UserProfile?> =
        session.currentAccountId.flatMapLatest { id ->
            if (id == null) flowOf(null) else dao.observeById(id).map { it?.toDomain() }
        }

    override suspend fun getProfile(): UserProfile? {
        val id = session.currentAccountId.first() ?: return null
        return dao.getById(id)?.toDomain()
    }

    override suspend fun upsertProfile(profile: UserProfile) {
        val id = dao.upsert(profile.toEntity())
        if (session.currentAccountId.first() == null) {
            session.setCurrentAccountId(id)
        }
    }

    override fun observeAccounts(): Flow<List<UserProfile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun createAccount(profile: UserProfile): Long {
        val id = dao.upsert(profile.copy(id = 0L).toEntity())
        session.setCurrentAccountId(id)
        return id
    }

    override suspend fun deleteAccount(id: Long) {
        dao.deleteById(id)
        val current = session.currentAccountId.first()
        if (current == id) {
            val next = dao.observeAll().first().firstOrNull()?.id
            session.setCurrentAccountId(next)
        }
    }

    override suspend fun setCurrentAccount(id: Long) {
        session.setCurrentAccountId(id)
    }

    override suspend fun getCurrentAccountId(): Long? {
        return session.currentAccountId.first()
    }
}

