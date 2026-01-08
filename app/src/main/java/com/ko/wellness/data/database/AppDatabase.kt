package com.ko.wellness.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ko.wellness.data.dao.ExerciseTemplateDao
import com.ko.wellness.data.dao.PlanExerciseDao
import com.ko.wellness.data.dao.WorkoutExerciseDao
import com.ko.wellness.data.dao.WorkoutPlanDao
import com.ko.wellness.data.entities.ExerciseTemplate
import com.ko.wellness.data.entities.PlanExercise
import com.ko.wellness.data.entities.WorkoutExercise
import com.ko.wellness.data.entities.WorkoutPlan

@Database(
    entities = [
        WorkoutExercise::class,
        ExerciseTemplate::class,
        WorkoutPlan::class,
        PlanExercise::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Provide Access to DAO
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun exerciseTemplateDao(): ExerciseTemplateDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun planExerciseDao(): PlanExerciseDao

    companion object {
        // Singleton Pattern - Only one Database Instance for the Entire App
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create exercise_templates Table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercise_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        exerciseType TEXT NOT NULL,
                        defaultSets INTEGER NOT NULL,
                        defaultReps INTEGER,
                        defaultDuration INTEGER
                    )
                """)

                // Create workout_plans table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS workout_plans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        isActive INTEGER NOT NULL
                    )
                """)

                // Create plan_exercises table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS plan_exercises (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        planId INTEGER NOT NULL,
                        exerciseTemplateId INTEGER NOT NULL,
                        dayOfWeek INTEGER NOT NULL,
                        `order` INTEGER NOT NULL,
                        sets INTEGER NOT NULL,
                        reps INTEGER,
                        duration INTEGER,
                        FOREIGN KEY(planId) REFERENCES workout_plans(id) ON DELETE CASCADE,
                        FOREIGN KEY(exerciseTemplateId) REFERENCES exercise_templates(id) ON DELETE CASCADE
                    )
                """)

                // Create indices for foreign keys
                database.execSQL("CREATE INDEX IF NOT EXISTS index_plan_exercises_planId ON plan_exercises(planId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_plan_exercises_exerciseTemplateId ON plan_exercises(exerciseTemplateId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wellness_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}