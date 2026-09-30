package com.example.intellipaatassignment.ui.dashboard

import com.example.intellipaatassignment.core.NoConnectivityException
import com.example.intellipaatassignment.domain.model.Course
import com.example.intellipaatassignment.domain.model.Lesson
import com.example.intellipaatassignment.domain.repository.AuthRepository
import com.example.intellipaatassignment.domain.repository.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val cachedCourse = Course(1, "Python Programming", "John Smith", 65, 20)

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `shows loading while the first refresh is in flight`() = runTest(dispatcher) {
        val viewModel = DashboardViewModel(FakeCourseRepository(), FakeAuthRepository())
        collect(viewModel)

        dispatcher.scheduler.runCurrent() // refresh started but not finished
        assertEquals(DashboardUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `offline with cached courses shows them with an offline notice`() = runTest(dispatcher) {
        val repository = FakeCourseRepository(
            initialCourses = listOf(cachedCourse),
            refreshResult = Result.failure(NoConnectivityException())
        )
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())
        collect(viewModel)
        advanceUntilIdle()

        val state = viewModel.uiState.value as DashboardUiState.Success
        assertEquals(listOf(cachedCourse), state.courses)
        assertNotNull(state.staleDataMessage)
    }

    @Test
    fun `offline with no cache shows an error`() = runTest(dispatcher) {
        val repository = FakeCourseRepository(refreshResult = Result.failure(NoConnectivityException()))
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())
        collect(viewModel)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DashboardUiState.Error)
    }

    @Test
    fun `successful refresh with no courses shows the empty state`() = runTest(dispatcher) {
        val viewModel = DashboardViewModel(FakeCourseRepository(), FakeAuthRepository())
        collect(viewModel)
        advanceUntilIdle()

        assertEquals(DashboardUiState.Empty(isRefreshing = false), viewModel.uiState.value)
    }

    @Test
    fun `retry after a failure loads the courses`() = runTest(dispatcher) {
        val repository = FakeCourseRepository(refreshResult = Result.failure(NoConnectivityException()))
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())
        collect(viewModel)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DashboardUiState.Error)

        repository.refreshResult = Result.success(Unit)
        repository.coursesOnNextRefresh = listOf(cachedCourse)
        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value as DashboardUiState.Success
        assertEquals(listOf(cachedCourse), state.courses)
        assertEquals(null, state.staleDataMessage)
    }

    /** uiState uses WhileSubscribed, so a collector must be active for it to update. */
    private fun TestScope.collect(viewModel: DashboardViewModel) {
        backgroundScope.launch { viewModel.uiState.collect {} }
    }

    private class FakeCourseRepository(
        initialCourses: List<Course> = emptyList(),
        var refreshResult: Result<Unit> = Result.success(Unit)
    ) : CourseRepository {
        private val courses = MutableStateFlow(initialCourses)
        var coursesOnNextRefresh: List<Course>? = null

        override fun observeCourses(): Flow<List<Course>> = courses
        override fun observeCourse(id: Int): Flow<Course?> = courses.map { list -> list.find { it.id == id } }
        override fun observeLessons(courseId: Int): Flow<List<Lesson>> = flowOf(emptyList())

        override suspend fun refreshCourses(): Result<Unit> {
            kotlinx.coroutines.delay(1_000)
            if (refreshResult.isSuccess) coursesOnNextRefresh?.let { courses.value = it }
            return refreshResult
        }

        override suspend fun markLessonCompleted(courseId: Int, lessonId: Int) = Unit
    }

    private class FakeAuthRepository : AuthRepository {
        override val isLoggedIn: Flow<Boolean> = flowOf(true)
        override suspend fun login(email: String, password: String) = Result.success(Unit)
        override suspend fun logout() = Unit
    }
}
