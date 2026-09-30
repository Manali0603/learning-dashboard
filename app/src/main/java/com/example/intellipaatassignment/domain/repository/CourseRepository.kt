package com.example.intellipaatassignment.domain.repository

import com.example.intellipaatassignment.domain.model.Course
import com.example.intellipaatassignment.domain.model.Lesson
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first course data. The observe* streams always read from the local cache;
 * [refreshCourses] pulls from the network into that cache and never clears it on failure.
 */
interface CourseRepository {
    fun observeCourses(): Flow<List<Course>>

    fun observeCourse(id: Int): Flow<Course?>

    fun observeLessons(courseId: Int): Flow<List<Lesson>>

    suspend fun refreshCourses(): Result<Unit>

    suspend fun markLessonCompleted(courseId: Int, lessonId: Int)
}
