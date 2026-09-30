package com.example.intellipaatassignment.data.mapper

import com.example.intellipaatassignment.data.local.CourseEntity
import com.example.intellipaatassignment.data.local.LessonEntity
import com.example.intellipaatassignment.data.remote.dto.CourseDto
import com.example.intellipaatassignment.domain.model.Course
import com.example.intellipaatassignment.domain.model.Lesson

fun CourseDto.toEntity() = CourseEntity(
    id = id,
    title = title,
    instructor = instructor,
    progress = progress.coerceIn(0, 100),
    lessonCount = lessons
)

fun CourseDto.lessonEntities(): List<LessonEntity> = lessonList.mapIndexed { index, lesson ->
    LessonEntity(id = lesson.id, courseId = id, title = lesson.title, position = index, completed = lesson.completed)
}

fun CourseEntity.toDomain() = Course(
    id = id,
    title = title,
    instructor = instructor,
    progress = progress,
    lessonCount = lessonCount
)

fun LessonEntity.toDomain() = Lesson(id = id, courseId = courseId, title = title, completed = completed)
