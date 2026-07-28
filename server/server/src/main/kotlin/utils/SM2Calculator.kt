package com.example.utils

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class SM2Result(
    val easeFactor: Double,
    val intervalDays: Int,
    val repetitions: Int,
    val dueDate: Instant
)

object SM2Calculator {

    private const val MIN_EASE_FACTOR = 1.3

    /**
     * Applies one SM-2 review step and returns the card's new schedule.
     * @param currentEaseFactor the card's ease_factor before this review
     * @param currentIntervalDays the card's interval_days before this review
     * @param currentRepetitions the card's repetitions before this review
     * @param quality recall quality rating, 0-5
     * @param now injectable clock point, defaults to real time (makes tests deterministic)
     */
    fun review(
        currentEaseFactor: Double,
        currentIntervalDays: Int,
        currentRepetitions: Int,
        quality: Int,
        now: Instant = Instant.now()
    ): SM2Result {
        require(quality in 0..5) { "quality must be between 0 and 5, got $quality" }

        val newRepetitions: Int
        val newIntervalDays: Int

        if (quality < 3) {
            // forgot: reset the streak
            newRepetitions = 0
            newIntervalDays = 1
        } else {
            newIntervalDays = when (currentRepetitions) {
                0 -> 1
                1 -> 6
                else -> (currentIntervalDays * currentEaseFactor).roundToInt()
            }
            newRepetitions = currentRepetitions + 1
        }

        // ease factor update always applies, regardless of pass/fail
        var newEaseFactor = currentEaseFactor +
                (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        if (newEaseFactor < MIN_EASE_FACTOR) {
            newEaseFactor = MIN_EASE_FACTOR
        }

        val newDueDate = now.plus(newIntervalDays.toLong(), ChronoUnit.DAYS)

        return SM2Result(
            easeFactor = newEaseFactor,
            intervalDays = newIntervalDays,
            repetitions = newRepetitions,
            dueDate = newDueDate
        )
    }
}