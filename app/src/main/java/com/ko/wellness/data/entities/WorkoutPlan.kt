package com.ko.wellness.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plans")
data class WorkoutPlan (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val isActive: Boolean = false // Only One Plan Can be Active at a Time
)