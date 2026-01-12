package com.ko.wellness.ui.workouts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.ko.wellness.R
import com.ko.wellness.data.database.AppDatabase
import com.ko.wellness.data.entities.ExerciseTemplate
import com.ko.wellness.databinding.DialogExerciseTemplateBinding
import com.ko.wellness.databinding.FragmentExerciseLibraryBinding
import kotlinx.coroutines.launch

class ExerciseLibraryFragment : Fragment() {

    private var _binding: FragmentExerciseLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExerciseTemplateAdapter
    private lateinit var database: AppDatabase

    private var currentCategory: String? = null // null = All

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupCategoryFilters()
        setupFab()
        loadExercises()
    }

    private fun setupRecyclerView() {
        adapter = ExerciseTemplateAdapter(
            onDeleteClick = { template ->
                showDeleteConfirmation(template)
            },
            onItemClick = { template ->
                showExerciseDialog(template)
            }
        )
        binding.recyclerExercises.adapter = adapter
    }

    private fun setupCategoryFilters() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            currentCategory = when (checkedIds.firstOrNull()) {
                R.id.chipWarmUp -> "Warm-Up"
                R.id.chipWorkout -> "Workout"
                R.id.chipCooldown -> "Cooldown"
                else -> null // All
            }
            loadExercises()
        }
    }

    private fun setupFab() {
        binding.fabAddExercise.setOnClickListener {
            showExerciseDialog(null) // null = New Exercise
        }
    }

    private fun loadExercises() {
        database.exerciseTemplateDao().getAllTemplatesLive().observe(viewLifecycleOwner) { allExercises ->
            val exercises = if (currentCategory == null) {
                allExercises
            } else {
                allExercises.filter { it.category == currentCategory }
            }

            adapter.submitList(exercises)

            // Show / Hide Empty State
            if(exercises.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerExercises.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerExercises.visibility = View.VISIBLE
            }
        }
    }

    private fun showExerciseDialog(existingTemplate: ExerciseTemplate?) {
        val dialogBinding = DialogExerciseTemplateBinding.inflate(layoutInflater)

        // Pre-Fill if Editing
        existingTemplate?.let { template ->
            dialogBinding.editExerciseName.setText(template.name)
            dialogBinding.editDescription.setText(template.description)

            // Set Category Radio Button
            when (template.category) {
                "Warm-Up" -> dialogBinding.radioWarmUp.isChecked = true
                "Workout" -> dialogBinding.radioWorkout.isChecked = true
                "Cooldown" -> dialogBinding.radioCooldown.isChecked = true
            }

            // Set Exercise Type and Defaults
            when(template.exerciseType) {
                "REPS" -> {
                    dialogBinding.radioReps.isChecked = true
                    dialogBinding.editSets.setText(template.defaultSets.toString())
                    dialogBinding.editReps.setText(template.defaultReps.toString())
                }
                "TIMED" -> {
                    dialogBinding.radioReps.isChecked = true
                    dialogBinding.editSetsTimed.setText(template.defaultSets.toString())
                    dialogBinding.editDuration.setText(template.defaultDuration.toString())
                }
            }
        }

        // Toggle Fields Based on Exercise Type
        dialogBinding.radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioReps) {
                dialogBinding.layoutRepsFields.visibility = View.VISIBLE
                dialogBinding.layoutTimedFields.visibility = View.GONE
            } else {
                dialogBinding.layoutRepsFields.visibility = View.GONE
                dialogBinding.layoutTimedFields.visibility = View.VISIBLE
            }
        }

        // Initial Visibility
        if (dialogBinding.radioReps.isChecked) {
            dialogBinding.layoutRepsFields.visibility = View.VISIBLE
            dialogBinding.layoutTimedFields.visibility = View.GONE
        } else {
            dialogBinding.layoutRepsFields.visibility = View.GONE
            dialogBinding.layoutTimedFields.visibility = View.VISIBLE
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (existingTemplate == null) "Add Exercise" else "Edit Exercise")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null) // Set to null to override later
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val name = dialogBinding.editExerciseName.text.toString().trim()
                if (name.isEmpty()) {
                    dialogBinding.layoutExerciseName.error = "Name is required"
                    return@setOnClickListener
                }

                val description = dialogBinding.editDescription.text.toString().trim()

                val category = when (dialogBinding.radioGroupCategory.checkedRadioButtonId) {
                    R.id.radioWarmUp -> "Warm-Up"
                    R.id.radioCooldown -> "Cooldown"
                    else -> "Workout"
                }

                val isReps = dialogBinding.radioReps.isChecked

                val template = if (isReps) {
                    val sets = dialogBinding.editSets.text.toString().toIntOrNull() ?: 3
                    val reps = dialogBinding.editReps.text.toString().toIntOrNull() ?: 10

                    ExerciseTemplate(
                        id = existingTemplate?.id ?: 0,
                        name = name,
                        description = description,
                        category = category,
                        exerciseType = "REPS",
                        defaultSets = sets,
                        defaultReps = reps,
                        defaultDuration = null
                    )
                } else {
                    val sets = dialogBinding.editSetsTimed.text.toString().toIntOrNull() ?: 3
                    val duration = dialogBinding.editDuration.text.toString().toIntOrNull() ?: 30

                    ExerciseTemplate(
                        id = existingTemplate?.id ?: 0,
                        name = name,
                        description = description,
                        category = category,
                        exerciseType = "TIMED",
                        defaultSets = sets,
                        defaultReps = null,
                        defaultDuration = duration
                    )
                }

                saveExercise(template)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun saveExercise(template: ExerciseTemplate) {
        lifecycleScope.launch {
            if (template.id == 0L) {
                // New exercise
                database.exerciseTemplateDao().insert(template)
                Snackbar.make(binding.root, "Exercise added", Snackbar.LENGTH_SHORT).show()
            } else {
                // Update existing
                database.exerciseTemplateDao().update(template)
                Snackbar.make(binding.root, "Exercise updated", Snackbar.LENGTH_SHORT).show()
            }
            loadExercises()
        }
    }

    private fun showDeleteConfirmation(template: ExerciseTemplate) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Exercise")
            .setMessage("Are you sure you want to delete \"${template.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                deleteExercise(template)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteExercise(template: ExerciseTemplate) {
        lifecycleScope.launch {
            database.exerciseTemplateDao().delete(template)
            Snackbar.make(binding.root, "Exercise deleted", Snackbar.LENGTH_SHORT).show()
            loadExercises()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}