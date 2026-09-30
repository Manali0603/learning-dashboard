package com.example.intellipaatassignment.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intellipaatassignment.domain.model.Course
import com.example.intellipaatassignment.ui.AppViewModelProvider
import com.example.intellipaatassignment.ui.components.CourseProgress
import com.example.intellipaatassignment.ui.components.StatusMessage
import com.example.intellipaatassignment.ui.theme.IntellipaatAssignmentTheme

@Composable
fun DashboardScreen(
    onCourseClick: (Int) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val loggedOut by viewModel.loggedOut.collectAsStateWithLifecycle()

    LaunchedEffect(loggedOut) {
        if (loggedOut) onLoggedOut()
    }

    DashboardContent(
        state = state,
        onRefresh = viewModel::refresh,
        onCourseClick = onCourseClick,
        onLogout = viewModel::logout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onRefresh: () -> Unit,
    onCourseClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Courses") },
                actions = { TextButton(onClick = onLogout) { Text("Logout") } }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (state) {
            DashboardUiState.Loading -> Box(contentModifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is DashboardUiState.Error -> StatusMessage(
                title = "Couldn't load courses",
                message = state.message,
                actionLabel = "Retry",
                onAction = onRefresh,
                modifier = contentModifier
            )

            is DashboardUiState.Empty -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = contentModifier
            ) {
                // Scrollable so pull-to-refresh works on the empty state too.
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    StatusMessage(
                        title = "No courses yet",
                        message = "You're not enrolled in any courses. Pull down to refresh.",
                        actionLabel = "Refresh",
                        onAction = onRefresh
                    )
                }
            }

            is DashboardUiState.Success -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = contentModifier
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.staleDataMessage != null) {
                        item(key = "stale-banner") { StaleDataBanner(state.staleDataMessage) }
                    }
                    items(state.courses, key = { it.id }) { course ->
                        CourseCard(course = course, onClick = { onCourseClick(course.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StaleDataBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "by ${course.instructor}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CourseProgress(course.progress)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${course.lessonCount} lessons", style = MaterialTheme.typography.bodySmall)
                Button(onClick = onClick) {
                    Text(if (course.progress >= 100) "Review" else "Continue")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    IntellipaatAssignmentTheme {
        DashboardContent(
            state = DashboardUiState.Success(
                courses = listOf(
                    Course(1, "Python Programming", "John Smith", 65, 20),
                    Course(2, "Generative AI", "Sarah Williams", 40, 15)
                ),
                isRefreshing = false,
                staleDataMessage = "You're offline. Showing courses saved on this device."
            ),
            onRefresh = {}, onCourseClick = {}, onLogout = {}
        )
    }
}
