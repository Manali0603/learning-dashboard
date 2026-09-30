package com.example.intellipaatassignment.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intellipaatassignment.core.NoConnectivityException
import com.example.intellipaatassignment.core.toUserMessage
import com.example.intellipaatassignment.domain.model.Course
import com.example.intellipaatassignment.domain.repository.AuthRepository
import com.example.intellipaatassignment.domain.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val courses: List<Course>,
        val isRefreshing: Boolean,
        /** Set when the latest refresh failed and we're showing cached data. */
        val staleDataMessage: String?
    ) : DashboardUiState

    data class Empty(val isRefreshing: Boolean) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}

class DashboardViewModel(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val refreshError = MutableStateFlow<Throwable?>(null)

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        courseRepository.observeCourses(),
        isRefreshing,
        refreshError
    ) { courses, refreshing, error ->
        when {
            courses.isNotEmpty() -> DashboardUiState.Success(courses, refreshing, error?.toStaleMessage())
            refreshing -> DashboardUiState.Loading
            error != null -> DashboardUiState.Error(error.toUserMessage())
            else -> DashboardUiState.Empty(isRefreshing = false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (isRefreshing.value) return
        isRefreshing.value = true
        viewModelScope.launch {
            val result = courseRepository.refreshCourses()
            refreshError.value = result.exceptionOrNull()
            isRefreshing.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loggedOut.value = true
        }
    }

    private fun Throwable.toStaleMessage(): String =
        if (this is NoConnectivityException) "You're offline. Showing courses saved on this device."
        else "Couldn't refresh. Showing saved courses."
}
