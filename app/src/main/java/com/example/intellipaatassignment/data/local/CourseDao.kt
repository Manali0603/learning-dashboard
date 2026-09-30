package com.example.intellipaatassignment.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.intellipaatassignment.domain.ProgressCalculator
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseDao {

    @Query("SELECT * FROM courses ORDER BY id")
    abstract fun observeCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    abstract fun observeCourse(courseId: Int): Flow<CourseEntity?>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY position")
    abstract fun observeLessons(courseId: Int): Flow<List<LessonEntity>>

    @Query("SELECT id FROM lessons WHERE completed = 1")
    abstract suspend fun getCompletedLessonIds(): List<Int>

    // @Upsert (not REPLACE): REPLACE deletes the old row first, which would cascade-delete lessons.
    @Upsert
    abstract suspend fun upsertCourses(courses: List<CourseEntity>)

    @Upsert
    abstract suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM courses WHERE id NOT IN (:keepIds)")
    abstract suspend fun deleteCoursesNotIn(keepIds: List<Int>)

    @Query("DELETE FROM courses")
    abstract suspend fun deleteAllCourses()

    @Query("UPDATE lessons SET completed = 1 WHERE id = :lessonId AND courseId = :courseId")
    abstract suspend fun setLessonCompleted(courseId: Int, lessonId: Int)

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId AND completed = 1")
    abstract suspend fun countCompletedLessons(courseId: Int): Int

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    abstract suspend fun countLessons(courseId: Int): Int

    @Query("UPDATE courses SET progress = :progress WHERE id = :courseId")
    abstract suspend fun updateProgress(courseId: Int, progress: Int)

    /** Marks a lesson done and recomputes the course's progress atomically. */
    @Transaction
    open suspend fun markLessonCompleted(courseId: Int, lessonId: Int) {
        setLessonCompleted(courseId, lessonId)
        val progress = ProgressCalculator.percent(countCompletedLessons(courseId), countLessons(courseId))
        updateProgress(courseId, progress)
    }

    /**
     * Replaces the cache with a fresh server snapshot, keeping lessons the user completed
     * locally (see [CourseSyncMerger]). Courses no longer on the server are removed.
     */
    @Transaction
    open suspend fun saveRemoteSnapshot(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        val merged = CourseSyncMerger.merge(courses, lessons, getCompletedLessonIds().toSet())
        if (merged.courses.isEmpty()) {
            deleteAllCourses()
        } else {
            deleteCoursesNotIn(merged.courses.map { it.id })
        }
        upsertCourses(merged.courses)
        upsertLessons(merged.lessons)
    }
}
