package com.ko.wellness.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_exercises")
data class WorkoutExercise (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: String,       // Format: "yyyy-MM-dd"
    val name: String,
    val category: String,   // "Warm-Up", "Workout", or "Cooldown"
    val sets: Int,
    val reps: Int? = null,
    val duration: Int? = null,
    val isCompleted: Boolean = false,
    val order: Int          // Display Order (0, 1, 2, etc.)
)