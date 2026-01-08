package com.ko.wellness.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExerciseTemplate (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val description: String,
    val category: String,
    val exerciseType: String, // "REPS" or "TIMED"
    val defaultSets: Int,
    val defaultReps: Int?,
    val defaultDuration: Int?
)