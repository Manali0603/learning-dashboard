package com.example.intellipaatassignment.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.intellipaatassignment.domain.repository.AuthRepository
import com.example.intellipaatassignment.ui.AppViewModelProvider
import com.example.intellipaatassignment.ui.dashboard.DashboardScreen
import com.example.intellipaatassignment.ui.details.CourseDetailsScreen
import com.example.intellipaatassignment.ui.login.LoginScreen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object DashboardRoute

@Serializable
data class CourseDetailsRoute(val courseId: Int)

/** Decides the start destination once, from the persisted session. null = still reading it. */
class SessionViewModel(authRepository: AuthRepository) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean?> = flow { emit(authRepository.isLoggedIn.first()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

@Composable
fun AppNavHost(
    sessionViewModel: SessionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val loggedIn = isLoggedIn
    if (loggedIn == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (loggedIn) DashboardRoute else LoginRoute
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<DashboardRoute> {
            DashboardScreen(
                onCourseClick = { courseId -> navController.navigate(CourseDetailsRoute(courseId)) },
                onLoggedOut = {
                    navController.navigate(LoginRoute) {
                        popUpTo<DashboardRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<CourseDetailsRoute> {
            CourseDetailsScreen(onBack = { navController.popBackStack() })
        }
    }
}
