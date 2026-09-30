package com.example.intellipaatassignment.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.intellipaatassignment.LearningApp
import com.example.intellipaatassignment.di.AppContainer
import com.example.intellipaatassignment.ui.dashboard.DashboardViewModel
import com.example.intellipaatassignment.ui.details.CourseDetailsViewModel
import com.example.intellipaatassignment.ui.login.LoginViewModel
import com.example.intellipaatassignment.ui.navigation.SessionViewModel

/** Builds every ViewModel from the [AppContainer]; the single seam between DI and the UI layer. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { SessionViewModel(container().authRepository) }
        initializer { LoginViewModel(container().authRepository) }
        initializer { DashboardViewModel(container().courseRepository, container().authRepository) }
        initializer {
            CourseDetailsViewModel(createSavedStateHandle(), container().courseRepository)
        }
    }

    private fun CreationExtras.container(): AppContainer =
        (this[APPLICATION_KEY] as LearningApp).container
}
