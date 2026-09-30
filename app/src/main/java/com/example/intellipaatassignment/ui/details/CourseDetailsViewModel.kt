package com.example.intellipaatassignment.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.intellipaatassignment.domain.model.Course
import com.example.intellipaatassignment.domain.model.Lesson
import com.example.intellipaatassignment.domain.repository.CourseRepository
import com.example.intellipaatassignment.ui.navigation.CourseDetailsRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CourseDetailsUiState {
    data object Loading : CourseDetailsUiState
    data object NotFound : CourseDetailsUiState
    data class Content(val course: Course, val lessons: List<Lesson>) : CourseDetailsUiState
}

class CourseDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val courseId: Int = savedStateHandle.toRoute<CourseDetailsRoute>().courseId

    // Both streams come from Room, so marking a lesson re-emits here and on the dashboard
    // without any manual refresh.
    val uiState: StateFlow<CourseDetailsUiState> = combine(
        courseRepository.observeCourse(courseId),
        courseRepository.observeLessons(courseId)
    ) { course, lessons ->
        if (course == null) CourseDetailsUiState.NotFound else CourseDetailsUiState.Content(course, lessons)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseDetailsUiState.Loading)

    fun onMarkCompleted(lessonId: Int) {
        viewModelScope.launch { courseRepository.markLessonCompleted(courseId, lessonId) }
    }
}
