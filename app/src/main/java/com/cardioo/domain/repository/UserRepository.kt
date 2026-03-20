package com.cardioo.domain.repository

import com.cardioo.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun upsertProfile(profile: UserProfile)
    fun observeAccounts(): Flow<List<UserProfile>>
    suspend fun createAccount(profile: UserProfile): Long
    suspend fun deleteAccount(id: Long)
    suspend fun setCurrentAccount(id: Long)
    suspend fun getCurrentAccountId(): Long?
}

