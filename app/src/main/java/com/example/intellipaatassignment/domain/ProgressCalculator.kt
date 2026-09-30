package com.example.intellipaatassignment.domain

import kotlin.math.roundToInt

object ProgressCalculator {
    fun percent(completed: Int, total: Int): Int =
        if (total <= 0) 0 else
            ((completed * 100.0) / total).roundToInt().coerceIn(0, 100)
}