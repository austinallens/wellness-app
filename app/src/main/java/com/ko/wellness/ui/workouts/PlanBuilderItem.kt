package com.ko.wellness.ui.workouts

import com.ko.wellness.data.entities.ExerciseTemplate
import com.ko.wellness.data.entities.PlanExercise

sealed interface PlanBuilderItem {
    data class Header(val category: String) : PlanBuilderItem
    data class Exercise(
        val planExercise: PlanExercise,
        val template: ExerciseTemplate
    ) : PlanBuilderItem
}