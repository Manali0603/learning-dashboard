package com.example.intellipaatassignment.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseSyncMergerTest {

    private val course = CourseEntity(id = 1, title = "Python", instructor = "John", progress = 25, lessonCount = 4)
    private val remoteLessons = listOf(
        LessonEntity(id = 101, courseId = 1, title = "Intro", position = 0, completed = true),
        LessonEntity(id = 102, courseId = 1, title = "Variables", position = 1, completed = false),
        LessonEntity(id = 103, courseId = 1, title = "Functions", position = 2, completed = false),
        LessonEntity(id = 104, courseId = 1, title = "OOP", position = 3, completed = false)
    )

    @Test
    fun `lesson completed offline survives a refresh from a server that doesn't know yet`() {
        val result = CourseSyncMerger.merge(listOf(course), remoteLessons, locallyCompletedLessonIds = setOf(102))

        assertTrue(result.lessons.single { it.id == 102 }.completed)
        // Progress is recomputed from the merged lessons (2 of 4), not taken from the server's 25.
        assertEquals(50, result.courses.single().progress)
    }

    @Test
    fun `server completion is kept even if not completed locally`() {
        val result = CourseSyncMerger.merge(listOf(course), remoteLessons, locallyCompletedLessonIds = emptySet())

        assertTrue(result.lessons.single { it.id == 101 }.completed)
        assertEquals(25, result.courses.single().progress)
    }

    @Test
    fun `course without lesson data keeps the server progress`() {
        val result = CourseSyncMerger.merge(listOf(course.copy(progress = 70)), emptyList(), emptySet())

        assertEquals(70, result.courses.single().progress)
    }
}
