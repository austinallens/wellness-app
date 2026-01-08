package com.ko.wellness.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ko.wellness.data.entities.WorkoutPlan

@Dao
interface WorkoutPlanDao {

    // Get all plans
    @Query("SELECT * FROM workout_plans ORDER BY name")
    fun getAllPlans(): LiveData<List<WorkoutPlan>>

    // Get the currently active plan
    @Query("SELECT * FROM workout_plans WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlan(): WorkoutPlan?

    // Get the currently active plan (LiveData)
    @Query("SELECT * FROM workout_plans WHERE isActive = 1 LIMIT 1")
    fun getActivePlanLive(): LiveData<WorkoutPlan?>

    // Get a single plan by ID
    @Query("SELECT * FROM workout_plans WHERE id = :id")
    suspend fun getPlanById(id: Long): WorkoutPlan?

    // Insert a plan
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: WorkoutPlan): Long

    // Update a plan
    @Update
    suspend fun update(plan: WorkoutPlan)

    // Delete a plan
    @Delete
    suspend fun delete(plan: WorkoutPlan)

    // Deactivate all plans (used before activating a new one)
    @Query("UPDATE workout_plans SET isActive = 0")
    suspend fun deactivateAllPlans()

    // Activate a specific plan
    @Transaction
    suspend fun activatePlan(planId: Long) {
        deactivateAllPlans()
        val plan = getPlanById(planId)
        plan?.let {
            update(it.copy(isActive = true))
        }
    }
}