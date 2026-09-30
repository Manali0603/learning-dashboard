package com.example.intellipaatassignment.ui.details

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intellipaatassignment.domain.model.Course
import com.example.intellipaatassignment.domain.model.Lesson
import com.example.intellipaatassignment.ui.AppViewModelProvider
import com.example.intellipaatassignment.ui.components.CourseProgress
import com.example.intellipaatassignment.ui.components.StatusMessage
import com.example.intellipaatassignment.ui.theme.IntellipaatAssignmentTheme

@Composable
fun CourseDetailsScreen(
    onBack: () -> Unit,
    viewModel: CourseDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CourseDetailsContent(state = state, onBack = onBack, onMarkCompleted = viewModel::onMarkCompleted)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailsContent(
    state: CourseDetailsUiState,
    onBack: () -> Unit,
    onMarkCompleted: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        (state as? CourseDetailsUiState.Content)?.course?.title ?: "Course",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (state) {
            CourseDetailsUiState.Loading -> Box(contentModifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            CourseDetailsUiState.NotFound -> StatusMessage(
                title = "Course not found",
                message = "This course is no longer available.",
                actionLabel = "Go back",
                onAction = onBack,
                modifier = contentModifier
            )

            is CourseDetailsUiState.Content -> LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(16.dp)
            ) {
                item(key = "header") { CourseHeader(state.course, state.lessons) }
                if (state.lessons.isEmpty()) {
                    item(key = "no-lessons") {
                        Text("No lessons available yet.", modifier = Modifier.padding(vertical = 16.dp))
                    }
                }
                itemsIndexed(state.lessons, key = { _, lesson -> lesson.id }) { index, lesson ->
                    LessonRow(number = index + 1, lesson = lesson, onMarkCompleted = { onMarkCompleted(lesson.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CourseHeader(course: Course, lessons: List<Lesson>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(course.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            "by ${course.instructor}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text("Current progress", style = MaterialTheme.typography.labelLarge)
        CourseProgress(course.progress)
        Text(
            "${lessons.count { it.completed }} of ${lessons.size} lessons completed",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        Text("Lessons", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun LessonRow(number: Int, lesson: Lesson, onMarkCompleted: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (lesson.completed) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Box(
                Modifier
                    .size(24.dp)
                    .padding(2.dp)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
        Column(Modifier.weight(1f)) {
            Text("$number. ${lesson.title}", style = MaterialTheme.typography.bodyLarge)
            Text(
                if (lesson.completed) "Completed" else "Pending",
                style = MaterialTheme.typography.bodySmall,
                color = if (lesson.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!lesson.completed) {
            OutlinedButton(onClick = onMarkCompleted) { Text("Mark done") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailsPreview() {
    IntellipaatAssignmentTheme {
        CourseDetailsContent(
            state = CourseDetailsUiState.Content(
                course = Course(1, "Python Programming", "John Smith", 50, 4),
                lessons = listOf(
                    Lesson(101, 1, "Introduction", true),
                    Lesson(102, 1, "Variables & Data Types", true),
                    Lesson(103, 1, "Functions", false),
                    Lesson(104, 1, "OOP", false)
                )
            ),
            onBack = {}, onMarkCompleted = {}
        )
    }
}
