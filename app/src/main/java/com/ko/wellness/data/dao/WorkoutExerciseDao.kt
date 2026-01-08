package com.ko.wellness.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ko.wellness.data.entities.WorkoutExercise

@Dao
interface WorkoutExerciseDao {

    // Get All Exercises for a Specific Date, Ordered by Category and Order
    @Query("SELECT * FROM workout_exercises WHERE date = :date ORDER BY 'order' ASC")
    fun getWorkoutsForDate(date: String): LiveData<List<WorkoutExercise>>

    // Insert One or More Exercises
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg exercises: WorkoutExercise)

    // Update an Existing Exercise
    @Update
    suspend fun update(exercise: WorkoutExercise)

    // Delete an Exercise
    @Delete
    suspend fun delete(exercise: WorkoutExercise)

    // Delete All Exercises for a Specific Date
    @Query("DELETE FROM workout_exercises WHERE date = :date")
    suspend fun deleteAllForDate(date: String)

    // Toggle Completion Status
    @Query("UPDATE workout_exercises SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    // Get Count of Exercises for a Date
    @Query("SELECT COUNT(*) FROM workout_exercises WHERE date = :date")
    suspend fun getWorkoutCountForDate(date: String): Int

    // Get Count of Completed Exercises for a Date
    @Query("SELECT COUNT(*) FROM workout_exercises WHERE date = :date AND isCompleted")
    suspend fun getCompletedCountForDate(date: String): Int
}