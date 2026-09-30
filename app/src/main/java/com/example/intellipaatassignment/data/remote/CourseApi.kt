package com.example.intellipaatassignment.data.remote

import android.content.res.AssetManager
import com.example.intellipaatassignment.core.NetworkMonitor
import com.example.intellipaatassignment.core.NoConnectivityException
import com.example.intellipaatassignment.data.remote.dto.CourseDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface CourseApi {
    suspend fun getCourses(): List<CourseDto>
}

/**
 * Stand-in for a real HTTP API: serves a JSON file from assets after a simulated delay.
 * It checks connectivity first, so turning the internet off behaves like a real
 * network failure and the offline cache path is genuinely exercised.
 */
class MockCourseApi(
    private val assets: AssetManager,
    private val networkMonitor: NetworkMonitor,
    private val json: Json,
    private val assetName: String = "courses.json",
    private val latencyMs: Long = 1_200,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CourseApi {

    override suspend fun getCourses(): List<CourseDto> = withContext(ioDispatcher) {
        delay(latencyMs)
        if (!networkMonitor.isOnline()) throw NoConnectivityException()
        val body = assets.open(assetName).bufferedReader().use { it.readText() }
        json.decodeFromString<List<CourseDto>>(body)
    }
}
