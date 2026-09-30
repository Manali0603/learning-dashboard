package com.example.intellipaatassignment.data.local

import com.example.intellipaatassignment.domain.ProgressCalculator

/**
 * Pure merge rule applied when a server snapshot arrives:
 * - a lesson is completed if the server OR this device says so (local progress is never lost
 *   just because the server hasn't received it yet);
 * - course progress is recomputed from the merged lessons, so it always matches what the
 *   details screen shows. Courses without lesson data keep the server's progress value.
 */
object CourseSyncMerger {

    data class Result(val courses: List<CourseEntity>, val lessons: List<LessonEntity>)

    fun merge(
        remoteCourses: List<CourseEntity>,
        remoteLessons: List<LessonEntity>,
        locallyCompletedLessonIds: Set<Int>
    ): Result {
        val lessons = remoteLessons.map { lesson ->
            if (lesson.id in locallyCompletedLessonIds) lesson.copy(completed = true) else lesson
        }
        val lessonsByCourse = lessons.groupBy { it.courseId }
        val courses = remoteCourses.map { course ->
            val courseLessons = lessonsByCourse[course.id].orEmpty()
            if (courseLessons.isEmpty()) {
                course
            } else {
                course.copy(
                    lessonCount = courseLessons.size,
                    progress = ProgressCalculator.percent(courseLessons.count { it.completed }, courseLessons.size)
                )
            }
        }
        return Result(courses, lessons)
    }
}
