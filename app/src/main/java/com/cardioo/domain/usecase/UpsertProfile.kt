package com.cardioo.domain.usecase

import com.cardioo.domain.model.UserProfile
import com.cardioo.domain.repository.UserRepository
import javax.inject.Inject

class UpsertProfile @Inject constructor(
    private val repo: UserRepository,
) {
    suspend operator fun invoke(profile: UserProfile) = repo.upsertProfile(profile)
}

