package com.example.intellipaatassignment.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.intellipaatassignment.core.AndroidNetworkMonitor
import com.example.intellipaatassignment.data.local.AppDatabase
import com.example.intellipaatassignment.data.remote.MockAuthApi
import com.example.intellipaatassignment.data.remote.MockCourseApi
import com.example.intellipaatassignment.data.repository.OfflineFirstCourseRepository
import com.example.intellipaatassignment.data.repository.SessionAuthRepository
import com.example.intellipaatassignment.data.session.KeystoreTokenCipher
import com.example.intellipaatassignment.data.session.SessionStore
import com.example.intellipaatassignment.domain.repository.AuthRepository
import com.example.intellipaatassignment.domain.repository.CourseRepository
import kotlinx.serialization.json.Json

private val Context.sessionDataStore by preferencesDataStore(name = "session")

/**
 * Manual dependency injection: one place that builds the object graph. Small enough that a DI
 * framework would add more ceremony than value; Hilt would replace this as the app grows.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val json = Json { ignoreUnknownKeys = true }

    private val networkMonitor = AndroidNetworkMonitor(appContext)

    private val database: AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "learning.db").build()

    private val sessionStore = SessionStore(appContext.sessionDataStore, KeystoreTokenCipher())

    val courseRepository: CourseRepository = OfflineFirstCourseRepository(
        // Swap to "courses_empty.json" to see the empty state.
        api = MockCourseApi(appContext.assets, networkMonitor, json, assetName = "courses.json"),
        dao = database.courseDao()
    )

    val authRepository: AuthRepository = SessionAuthRepository(
        api = MockAuthApi(networkMonitor),
        sessionStore = sessionStore,
        courseDao = database.courseDao()
    )
}
