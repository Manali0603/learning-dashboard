package com.example.intellipaatassignment.data.repository

import com.example.intellipaatassignment.core.suspendRunCatching
import com.example.intellipaatassignment.data.local.CourseDao
import com.example.intellipaatassignment.data.mapper.lessonEntities
import com.example.intellipaatassignment.data.mapper.toDomain
import com.example.intellipaatassignment.data.mapper.toEntity
import com.example.intellipaatassignment.data.remote.CourseApi
import com.example.intellipaatassignment.domain.model.Course
import com.example.intellipaatassignment.domain.model.Lesson
import com.example.intellipaatassignment.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room is the single source of truth; the network only ever writes into it. */
class OfflineFirstCourseRepository(
    private val api: CourseApi,
    private val dao: CourseDao
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        dao.observeCourses().map { courses -> courses.map { it.toDomain() } }

    override fun observeCourse(id: Int): Flow<Course?> =
        dao.observeCourse(id).map { it?.toDomain() }

    override fun observeLessons(courseId: Int): Flow<List<Lesson>> =
        dao.observeLessons(courseId).map { lessons -> lessons.map { it.toDomain() } }

    override suspend fun refreshCourses(): Result<Unit> = suspendRunCatching {
        val remote = api.getCourses()
        dao.saveRemoteSnapshot(
            courses = remote.map { it.toEntity() },
            lessons = remote.flatMap { it.lessonEntities() }
        )
    }

    // Local-only for the assignment. In production this would also enqueue the change in an
    // outbox table and let WorkManager sync it to the server when online.
    override suspend fun markLessonCompleted(courseId: Int, lessonId: Int) {
        dao.markLessonCompleted(courseId, lessonId)
    }
}
