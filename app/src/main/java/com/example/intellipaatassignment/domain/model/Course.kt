package com.example.intellipaatassignment.domain.model

data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessonCount: Int
)

data class Lesson(
    val id: Int,
    val courseId: Int,
    val title: String,
    val completed: Boolean
)
