package com.ko.wellness.ui.workouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ko.wellness.data.database.AppDatabase
import com.ko.wellness.data.entities.WorkoutExercise
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
    ): View {
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

        // Generate Workouts from active Plan
        generateDailyWorkoutsFromPlan(today)

        // Observe Workouts for Today
        database.workoutExerciseDao().getWorkoutsForDate(today).observe(viewLifecycleOwner) { exercises ->
            // Convert Exercise to WorkoutItems with Headers
            val items = groupExercisesByCategory(exercises)
            adapter.submitList(items)

            // Update Progress Text
            updateProgress(exercises)
        }

        database.workoutExerciseDao().getWorkoutsForDate(today).observe(viewLifecycleOwner) { exercises ->
            val items = groupExercisesByCategory(exercises)
            adapter.submitList(items)
            updateProgress(exercises)

            // Show/Hide Play Button
            if (exercises.isNotEmpty()) {
                binding.fabStartWorkout.visibility = View.VISIBLE
                binding.fabStartWorkout.setOnClickListener {
                    startWorkout(today)
                }
            } else {
                binding.fabStartWorkout.visibility = View.GONE
            }
        }
    }

    private fun startWorkout(date: String) {
        val intent = android.content.Intent(requireContext(), WorkoutPlayerActivity::class.java)
        intent.putExtra("WORKOUT_DATE", date)
        startActivity(intent)
    }

    private fun generateDailyWorkoutsFromPlan(date: String) {
        lifecycleScope.launch {
            // Check if Workouts Already Exist for Today
            val existingCount = database.workoutExerciseDao().getWorkoutCountForDate(date)
            if (existingCount > 0) {
                // Already Have Workouts for Today
                return@launch
            }

            // Get Active Plan
            val activePlan = database.workoutPlanDao().getActivePlan()
            if (activePlan == null) {
                // No Active PLan - Show Empty State
                return@launch
            }

            // Get Today's Day of Week (Calendar.MONDAY = 2, etc.)
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            // Get Exercises for this day from the Plan
            val planExercises = database.planExerciseDao()
                .getExercisesForPlanAndDay(activePlan.id, dayOfWeek)

            if (planExercises.isEmpty()) {
                // No Exercises Scheduled for Today
                return@launch
            }

            // Convert PlanExercise to WorkoutExercise
            val workoutExercises = planExercises.mapNotNull { planExercise ->
                val template = database.exerciseTemplateDao()
                    .getTemplateById(planExercise.exerciseTemplateId)

                template?.let {
                    WorkoutExercise(
                        id = 0,
                        date = date,
                        name = template.name,
                        category = template.category,
                        sets = planExercise.sets,
                        reps = planExercise.reps,
                        duration = planExercise.duration,
                        isCompleted = false,
                        order = planExercise.order
                    )
                }
            }

            // Insert All Workout Exercises for Today
            if (workoutExercises.isNotEmpty()) {
                database.workoutExerciseDao().insert(*workoutExercises.toTypedArray())
            }
        }
    }

    private fun updateExerciseCompletion(exerciseId: Long, isCompleted: Boolean) {
        lifecycleScope.launch {
            database.workoutExerciseDao().updateCompletionStatus(exerciseId, isCompleted)
        }
    }

    private fun updateProgress(exercise: List<WorkoutExercise>) {
        val total = exercise.size
        val completed = exercise.count { it.isCompleted }

        if (total == 0) {
            binding.tvProgress.text = "No workouts scheduled for today"
            binding.progressBar.progress = 0
        } else {
            val percentage = (completed * 100) / total
            binding.tvProgress.text = "$completed of $total completed ($percentage%)"
            binding.progressBar.progress = percentage
        }
    }

    private fun groupExercisesByCategory(
        exercises: List<WorkoutExercise>
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}