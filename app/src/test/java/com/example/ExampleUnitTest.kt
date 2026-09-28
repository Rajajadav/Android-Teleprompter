package com.example

import com.example.util.Formatters
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun readingTimeEstimation_isCorrect() {
        val time = Formatters.formatEstimatedReadingTime(130, 1.0f)
        assertEquals("1m 0s", time)
    }

    @Test
    fun durationFormatting_isCorrect() {
        val formatted = Formatters.formatDuration(125)
        assertEquals("02:05", formatted)
    }
}
