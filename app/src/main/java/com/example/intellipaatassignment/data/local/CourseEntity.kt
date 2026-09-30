package com.example.intellipaatassignment.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessonCount: Int
)

@Entity(
    tableName = "lessons",
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = CASCADE)],
    indices = [Index("courseId")]
)
data class LessonEntity(
    @PrimaryKey val id: Int, val courseId: Int, val title: String,
    val position: Int, val completed: Boolean
)
