package com.ko.wellness.ui.workouts

import com.ko.wellness.data.entities.WorkoutExercise

sealed class WorkoutPlayerState {
    data class Exercise(
        val exercise: WorkoutExercise,
        val currentSet: Int,
        val totalSets: Int,
        val isRest: Boolean = false
    ) : WorkoutPlayerState()

    object Completed : WorkoutPlayerState()
}