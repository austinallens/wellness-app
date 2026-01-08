package com.ko.wellness.ui.workouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ko.wellness.data.database.AppDatabase
import com.ko.wellness.databinding.FragmentTodayBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private  lateinit var database: AppDatabase
    private lateinit var adapter: WorkoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Database
        database = AppDatabase.getDatabase(requireContext())

        // Set Up RecyclerView Adapter
        adapter = WorkoutAdapter { exerciseId, isComplete ->
            // Handle Checkbox Clicks
            updateExerciseCompletion(exerciseId, isComplete)
        }
        binding.recyclerView.adapter = adapter

        // Get Today's Date
        val today = getTodayDate()

        // Display Formatted Date
        binding.tvDate.text = getFormattedDate()

        // Observe Workouts for Today
        database.workoutExerciseDao().getWorkoutsForDate(today).observe(viewLifecycleOwner) { exercises ->
            // Convert Exercise to WorkoutItems with Headers
            val items = groupExercisesByCategory(exercises)
            adapter.submitList(items)

            // Update Progress Text
            updateProgress(exercises)
        }

        // TODO: Remove this after testing - adds sample data
        addSampleDataIfEmpty(today)
    }

    private fun updateExerciseCompletion(exerciseId: Long, isCompleted: Boolean) {
        lifecycleScope.launch {
            database.workoutExerciseDao().updateCompletionStatus(exerciseId, isCompleted)
        }
    }

    private fun updateProgress(exercise: List<com.ko.wellness.data.entities.WorkoutExercise>) {
        val total = exercise.size
        val completed = exercise.count { it.isCompleted }

        if (total == 0) {
            binding.tvProgress.text = "No workouts scheduled for today"
            binding.progressBar.progress = 0
            binding.progressBar.max = 100
        } else {
            val percentage = (completed * 100) / total
            binding.tvProgress.text = "$completed of $total completed ($percentage%)"
            binding.progressBar.max = 100
            binding.progressBar.progress = percentage
        }
    }

    private fun groupExercisesByCategory(
        exercises: List<com.ko.wellness.data.entities.WorkoutExercise>
    ): List<WorkoutItem> {
        val items = mutableListOf<WorkoutItem>()

        // Define Category Order
        val categories = listOf("Warm-Up", "Workout", "Cooldown")

        for (category in categories) {
            val categoryExercises = exercises.filter { it.category == category }
            if (categoryExercises.isNotEmpty()) {
                // Add Header
                items.add(WorkoutItem.Header(category))
                // Add Exercises
                categoryExercises.forEach { exercise ->
                    items.add(WorkoutItem.Exercise(exercise))
                }
            }
        }

        return items
    }

    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun addSampleDataIfEmpty(date: String) {
        lifecycleScope.launch {
            val count = database.workoutExerciseDao().getWorkoutCountForDate(date)
            if (count == 0) {
                // Add Sample Exercises
                database.workoutExerciseDao().insert(
                    com.ko.wellness.data.entities.WorkoutExercise(
                        date = date,
                        name = "Jumping Jacks",
                        category = "Warm-Up",
                        sets = 1,
                        duration = 60,
                        order = 0
                    ),
                    com.ko.wellness.data.entities.WorkoutExercise(
                        date = date,
                        name = "Arm Circles",
                        category = "Warm-Up",
                        sets = 1,
                        duration = 30,
                        order = 1
                    ),
                    com.ko.wellness.data.entities.WorkoutExercise(
                        date = date,
                        name = "Push-ups",
                        category = "Workout",
                        sets = 3,
                        reps = 10,
                        order = 2
                    ),
                    com.ko.wellness.data.entities.WorkoutExercise(
                        date = date,
                        name = "Squats",
                        category = "Workout",
                        sets = 3,
                        reps = 15,
                        order = 3
                    ),
                    com.ko.wellness.data.entities.WorkoutExercise(
                        date = date,
                        name = "Plank",
                        category = "Workout",
                        sets = 3,
                        duration = 30,
                        order = 4
                    ),
                    com.ko.wellness.data.entities.WorkoutExercise(
                        date = date,
                        name = "Stretching",
                        category = "Cooldown",
                        sets = 1,
                        duration = 300,
                        order = 5
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}