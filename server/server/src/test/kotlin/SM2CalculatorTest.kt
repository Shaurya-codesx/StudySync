package com.example

import com.example.utils.SM2Calculator
import junit.framework.Assert.assertEquals
import kotlin.test.assertFailsWith
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class SM2CalculatorTest {

    private val fixedNow: Instant = Instant.parse("2026-07-28T00:00:00Z")
    private val delta = 0.0001

    @Test
    fun `remembered easily streak grows interval and ease factor correctly`() {
        // start: fresh card, ease_factor 2.5, interval 0, repetitions 0
        var ease = 2.5
        var interval = 0
        var reps = 0

        // review 1 — quality 5
        var result = SM2Calculator.review(ease, interval, reps, quality = 5, now = fixedNow)
        assertEquals(1, result.intervalDays)
        assertEquals(1, result.repetitions)
        assertEquals(2.6, result.easeFactor, delta)
        assertEquals(fixedNow.plus(1, ChronoUnit.DAYS), result.dueDate)

        ease = result.easeFactor; interval = result.intervalDays; reps = result.repetitions

        // review 2 — quality 5
        result = SM2Calculator.review(ease, interval, reps, quality = 5, now = fixedNow)
        assertEquals(6, result.intervalDays)
        assertEquals(2, result.repetitions)
        assertEquals(2.7, result.easeFactor, delta)

        ease = result.easeFactor; interval = result.intervalDays; reps = result.repetitions

        // review 3 — quality 5 -> interval = round(6 * 2.7) = 16
        result = SM2Calculator.review(ease, interval, reps, quality = 5, now = fixedNow)
        assertEquals(16, result.intervalDays)
        assertEquals(3, result.repetitions)
        assertEquals(2.8, result.easeFactor, delta)
    }

    @Test
    fun `forgetting a card resets repetitions and interval but does not reset ease factor`() {
        // card with some review history: ease 2.8, interval 16, repetitions 3
        val result = SM2Calculator.review(
            currentEaseFactor = 2.8,
            currentIntervalDays = 16,
            currentRepetitions = 3,
            quality = 2, // < 3 => forgot
            now = fixedNow
        )

        assertEquals(0, result.repetitions)
        assertEquals(1, result.intervalDays)
        // 2.8 + (0.1 - 3*(0.08 + 3*0.02)) = 2.8 - 0.32 = 2.48
        assertEquals(2.48, result.easeFactor, delta)
        assertEquals(fixedNow.plus(1, ChronoUnit.DAYS), result.dueDate)
    }

    @Test
    fun `ease factor never drops below the 1_3 floor`() {
        // low starting ease factor, worst possible quality rating
        val result = SM2Calculator.review(
            currentEaseFactor = 1.5,
            currentIntervalDays = 0,
            currentRepetitions = 0,
            quality = 0,
            now = fixedNow
        )

        // raw formula would give 1.5 - 0.8 = 0.7, must clamp to 1.3
        assertEquals(1.3, result.easeFactor, delta)
        assertEquals(0, result.repetitions)
        assertEquals(1, result.intervalDays)
    }

    @Test
    fun `rejects an out-of-range quality value`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            SM2Calculator.review(2.5, 0, 0, quality = 6, now = fixedNow)
        }
        assertEquals("quality must be between 0 and 5, got 6", ex.message)
    }
}