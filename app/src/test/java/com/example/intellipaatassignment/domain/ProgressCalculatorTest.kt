package com.example.intellipaatassignment.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {

    @Test
    fun `matches the seeded course data`() {
        assertEquals(65, ProgressCalculator.percent(completed = 13, total = 20))
        assertEquals(40, ProgressCalculator.percent(completed = 6, total = 15))
        assertEquals(25, ProgressCalculator.percent(completed = 7, total = 28))
    }

    @Test
    fun `rounds to the nearest percent`() {
        assertEquals(33, ProgressCalculator.percent(completed = 1, total = 3))
        assertEquals(67, ProgressCalculator.percent(completed = 2, total = 3))
    }

    @Test
    fun `course without lessons is zero instead of dividing by zero`() {
        assertEquals(0, ProgressCalculator.percent(completed = 0, total = 0))
    }

    @Test
    fun `is clamped to 0-100`() {
        assertEquals(100, ProgressCalculator.percent(completed = 5, total = 4))
        assertEquals(0, ProgressCalculator.percent(completed = -1, total = 4))
    }
}
