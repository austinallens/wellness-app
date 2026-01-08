package com.ko.wellness.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plan_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseTemplate::class,
            parentColumns = ["id"],
            childColumns = ["exerciseTemplateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId"), Index("exerciseTemplateId")]
)
data class PlanExercise (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val planId: Long,
    val exerciseTemplateId: Long,
    val dayOfWeek: Int,
    val order: Int,

    // Can Override the template's defaults for this Specific Plan
    val sets: Int,
    val reps: Int? = null,
    val duration: Int? = null
)