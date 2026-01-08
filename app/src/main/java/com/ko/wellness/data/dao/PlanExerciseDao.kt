package com.ko.wellness.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ko.wellness.data.entities.PlanExercise

@Dao
interface PlanExerciseDao {

    // Get all exercises for a specific plan
    @Query("SELECT * FROM plan_exercises WHERE planId = :planId ORDER BY dayOfWeek, `order`")
    fun getExercisesForPlan(planId: Long): LiveData<List<PlanExercise>>

    // Get exercises for a specific plan and day
    @Query("SELECT * FROM plan_exercises WHERE planId = :planId AND dayOfWeek = :dayOfWeek ORDER BY `order`")
    suspend fun getExercisesForPlanAndDay(planId: Long, dayOfWeek: Int): List<PlanExercise>

    // Insert one or more plan exercises
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg planExercises: PlanExercise)

    // Update a plan exercise
    @Update
    suspend fun update(planExercise: PlanExercise)

    // Delete a plan exercise
    @Delete
    suspend fun delete(planExercise: PlanExercise)

    // Delete all exercises for a plan
    @Query("DELETE FROM plan_exercises WHERE planId = :planId")
    suspend fun deleteAllForPlan(planId: Long)

    // Delete all exercises for a plan and specific day
    @Query("DELETE FROM plan_exercises WHERE planId = :planId AND dayOfWeek = :dayOfWeek")
    suspend fun deleteAllForPlanAndDay(planId: Long, dayOfWeek: Int)

    // Get count of exercises for a plan
    @Query("SELECT COUNT(*) FROM plan_exercises WHERE planId = :planId")
    suspend fun getExerciseCountForPlan(planId: Long): Int
}