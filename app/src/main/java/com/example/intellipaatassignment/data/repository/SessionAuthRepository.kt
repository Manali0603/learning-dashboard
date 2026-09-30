package com.example.intellipaatassignment.data.repository

import com.example.intellipaatassignment.core.suspendRunCatching
import com.example.intellipaatassignment.data.local.CourseDao
import com.example.intellipaatassignment.data.remote.AuthApi
import com.example.intellipaatassignment.data.session.SessionStore
import com.example.intellipaatassignment.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class SessionAuthRepository(
    private val api: AuthApi,
    private val sessionStore: SessionStore,
    private val courseDao: CourseDao
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> =
        sessionStore.token.map { it != null }.distinctUntilChanged()

    override suspend fun login(email: String, password: String): Result<Unit> = suspendRunCatching {
        val token = api.login(email.trim(), password)
        sessionStore.saveToken(token)
    }

    override suspend fun logout() {
        sessionStore.clear()
        // The cache holds this user's progress; don't leave it behind for the next account.
        courseDao.deleteAllCourses()
    }
}
