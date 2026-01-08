package com.ko.wellness.data.entities

enum class ExerciseType {
    REPS, // For Exercises Measured by Repetitions
    TIMED; // For Exercises Measured by Duration

    companion object {
        fun fromString(value: String): ExerciseType {
            return valueOf(value)
        }
    }
}