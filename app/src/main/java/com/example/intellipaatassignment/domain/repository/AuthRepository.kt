package com.example.intellipaatassignment.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>

    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun logout()
}
