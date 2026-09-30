package com.example.intellipaatassignment.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessons: Int,
    val lessonList: List<LessonDto> = emptyList()
)

@Serializable
data class LessonDto(
    val id: Int,
    val title: String,
    val completed: Boolean
)
