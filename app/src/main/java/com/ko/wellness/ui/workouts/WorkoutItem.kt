package com.ko.wellness.ui.workouts

import com.ko.wellness.data.entities.WorkoutExercise

sealed class WorkoutItem {
    data class Header(val category: String) : WorkoutItem()
    data class Exercise(val exercise: WorkoutExercise) : WorkoutItem()
}