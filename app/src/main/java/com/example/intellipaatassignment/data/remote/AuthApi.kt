package com.example.intellipaatassignment.data.remote

import com.example.intellipaatassignment.core.InvalidCredentialsException
import com.example.intellipaatassignment.core.NetworkMonitor
import com.example.intellipaatassignment.core.NoConnectivityException
import kotlinx.coroutines.delay
import java.util.UUID

interface AuthApi {
    /** Returns an access token for valid credentials. */
    suspend fun login(email: String, password: String): String
}

class MockAuthApi(
    private val networkMonitor: NetworkMonitor,
    private val latencyMs: Long = 1_000
) : AuthApi {

    override suspend fun login(email: String, password: String): String {
        delay(latencyMs)
        if (!networkMonitor.isOnline()) throw NoConnectivityException()
        if (!email.equals(DEMO_EMAIL, ignoreCase = true) || password != DEMO_PASSWORD) {
            throw InvalidCredentialsException()
        }
        return "mock-token-${UUID.randomUUID()}"
    }

    companion object {
        const val DEMO_EMAIL = "learner@example.com"
        const val DEMO_PASSWORD = "password123"
    }
}
