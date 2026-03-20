package com.cardioo.domain.usecase

import com.cardioo.domain.model.UserProfile
import com.cardioo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProfile @Inject constructor(
    private val repo: UserRepository,
) {
    operator fun invoke(): Flow<UserProfile?> = repo.observeProfile()
}

