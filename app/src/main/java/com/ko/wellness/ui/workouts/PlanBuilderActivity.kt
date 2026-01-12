package com.ko.wellness.ui.workouts

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import com.ko.wellness.R
import com.ko.wellness.data.database.AppDatabase
import com.ko.wellness.data.entities.ExerciseTemplate
import com.ko.wellness.data.entities.PlanExercise
import com.ko.wellness.databinding.ActivityPlanBuilderBinding
import com.ko.wellness.databinding.DialogExercisePickerBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class PlanBuilderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanBuilderBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: PlanExerciseAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private var planId: Long = 0
    private var planName: String = ""
    private var currentDay: Int = Calendar.MONDAY

    private var currentExercises = mutableListOf<PlanBuilderItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanBuilderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get plan ID and name from intent
        planId = intent.getLongExtra("PLAN_ID", 0)
        planName = intent.getStringExtra("PLAN_NAME") ?: "Workout Plan"

        database = AppDatabase.getDatabase(this)

        setupToolbar()
        setupRecyclerView()
        setupDayChips()
        setupFab()

        // Load exercises for Monday by default
        loadExercisesForDay(currentDay)
    }

    private fun setupToolbar() {
        binding.toolbar.title = planName
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PlanExerciseAdapter(
            onDeleteClick = { exerciseItem ->
                deletePlanExercise(exerciseItem.planExercise)
            },
            onMoveItem = { fromPosition, toPosition ->
                moveExercise(fromPosition, toPosition)
            }
        )
        binding.recyclerDayExercises.adapter = adapter

        // Set up drag and drop
        val callback = PlanExerciseItemTouchHelper(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerDayExercises)
    }

    private fun setupDayChips() {
        binding.chipGroupDays.setOnCheckedStateChangeListener { _, checkedIds ->
            currentDay = when (checkedIds.firstOrNull()) {
                R.id.chipMonday -> Calendar.MONDAY
                R.id.chipTuesday -> Calendar.TUESDAY
                R.id.chipWednesday -> Calendar.WEDNESDAY
                R.id.chipThursday -> Calendar.THURSDAY
                R.id.chipFriday -> Calendar.FRIDAY
                R.id.chipSaturday -> Calendar.SATURDAY
                R.id.chipSunday -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
            loadExercisesForDay(currentDay)
        }
    }

    private fun setupFab() {
        binding.fabAddExercise.setOnClickListener {
            showExercisePicker()
        }
    }

    private fun loadExercisesForDay(dayOfWeek: Int) {
        lifecycleScope.launch {
            val planExercises = database.planExerciseDao()
                .getExercisesForPlanAndDay(planId, dayOfWeek)

            // Get template for each exercise
            val exercisesWithTemplates = planExercises.mapNotNull { planExercise ->
                val template = database.exerciseTemplateDao()
                    .getTemplateById(planExercise.exerciseTemplateId)
                template?.let { planExercise to it }
            }

            // Group by Category and Create Items with Headers
            val items = mutableListOf<PlanBuilderItem>()
            val categories = listOf("Warm-Up", "Workout", "Cooldown")

            for (category in categories) {
                val categoryExercises = exercisesWithTemplates
                    .filter { it.second.category == category }
                    .sortedBy { it.first.order }

                if (categoryExercises.isNotEmpty()) {
                    items.add(PlanBuilderItem.Header(category))
                    categoryExercises.forEach { (planExercise, template) ->
                        items.add(PlanBuilderItem.Exercise(planExercise, template))
                    }
                }
            }

            currentExercises = items.toMutableList()
            adapter.submitList(items)

            // Show/hide empty state
            if (exercisesWithTemplates.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerDayExercises.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerDayExercises.visibility = View.VISIBLE
            }
        }
    }

    private fun moveExercise(fromPosition: Int, toPosition: Int) {
        // Swap in the list
        val item = currentExercises.removeAt(fromPosition)
        currentExercises.add(toPosition, item)
        adapter.submitList(currentExercises.toList())

        // Update order in database
        lifecycleScope.launch {
            // Get all exercises (skip headers) and update their order
            val exercises = currentExercises.filterIsInstance<PlanBuilderItem.Exercise>()
            exercises.forEachIndexed { index, exerciseItem ->
                val updated = exerciseItem.planExercise.copy(order = index)
                database.planExerciseDao().update(updated)
            }
        }
    }

    private fun showExercisePicker() {
        val dialogBinding = DialogExercisePickerBinding.inflate(layoutInflater)

        val pickerAdapter = ExercisePickerAdapter { template ->
            addExerciseToPlan(template)
        }

        dialogBinding.recyclerExercises.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        dialogBinding.recyclerExercises.adapter = pickerAdapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Exercise")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .create()

        lifecycleScope.launch {
            // Use Suspend Version for One-Time Query
            val allTemplates = database.exerciseTemplateDao().getAllTemplates()
            pickerAdapter.submitList(allTemplates)

            // Search Functionality
            dialogBinding.editSearch.addTextChangedListener { text ->
                val query = text.toString().lowercase()
                val filtered = allTemplates.filter {
                    it.name.lowercase().contains(query)
                }
                pickerAdapter.submitList(filtered)
            }
        }

        dialog.show()
    }

    private fun addExerciseToPlan(template: ExerciseTemplate) {
        lifecycleScope.launch {
            // Get current max order for this day
            val existingExercises = database.planExerciseDao()
                .getExercisesForPlanAndDay(planId, currentDay)

            // Get Max Order for this Category
            val sameCategory = existingExercises.mapNotNull { pe ->
                val t = database.exerciseTemplateDao().getTemplateById(pe.exerciseTemplateId)
                if (t?.category == template.category) pe else null
            }
            val maxOrder = sameCategory.maxOfOrNull { it.order } ?: -1

            // Create new plan exercise with template defaults
            val planExercise = PlanExercise(
                id = 0,
                planId = planId,
                exerciseTemplateId = template.id,
                dayOfWeek = currentDay,
                order = maxOrder + 1,
                sets = template.defaultSets,
                reps = template.defaultReps,
                duration = template.defaultDuration
            )

            database.planExerciseDao().insert(planExercise)

            Snackbar.make(
                binding.root,
                "${template.name} added",
                Snackbar.LENGTH_SHORT
            ).show()

            // Reload exercises
            loadExercisesForDay(currentDay)
        }
    }

    private fun deletePlanExercise(planExercise: PlanExercise) {
        lifecycleScope.launch {
            database.planExerciseDao().delete(planExercise)
            Snackbar.make(binding.root, "Exercise removed", Snackbar.LENGTH_SHORT).show()
            loadExercisesForDay(currentDay)
        }
    }
}