package com.ko.wellness.data.dao

import androidx.room.*
import com.ko.wellness.data.entities.ExerciseTemplate

@Dao
interface ExerciseTemplateDao {
    @Insert
    suspend fun insert(template: ExerciseTemplate): Long

    @Update
    suspend fun update(template: ExerciseTemplate)

    @Delete
    suspend fun delete(template: ExerciseTemplate)

    @Query("SELECT * FROM ExerciseTemplate ORDER BY category, name")
    suspend fun getAllTemplates(): List<ExerciseTemplate>

    @Query("SELECT * FROM ExerciseTemplate WHERE category = :category ORDER BY name")
    suspend fun getTemplatesByCategory(category: String): List<ExerciseTemplate>

    @Query("SELECT * FROM ExerciseTemplate WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name")
    suspend fun searchTemplates(searchQuery: String): List<ExerciseTemplate>

    @Query("SELECT * FROM ExerciseTemplate WHERE id = :id")
    suspend fun getTemplateById(id: Long): ExerciseTemplate?
}