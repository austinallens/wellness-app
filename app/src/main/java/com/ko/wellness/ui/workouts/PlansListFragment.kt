package com.ko.wellness.ui.workouts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.ko.wellness.data.database.AppDatabase
import com.ko.wellness.data.entities.WorkoutPlan
import com.ko.wellness.databinding.DialogPlanNameBinding
import com.ko.wellness.databinding.FragmentPlansListBinding
import kotlinx.coroutines.launch

class PlansListFragment : Fragment() {

    private var _binding: FragmentPlansListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WorkoutPlanAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlansListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupFab()
        loadPlans()
    }

    private fun setupRecyclerView() {
        adapter = WorkoutPlanAdapter(
            onEditClick = { plan ->
                // Navigate to Plan Builder
                val intent = android.content.Intent(requireContext(), PlanBuilderActivity::class.java)
                intent.putExtra("PLAN_ID", plan.id)
                intent.putExtra("PLAN_NAME", plan.name)
                startActivity(intent)
            },
            onActivateClick = { plan ->
                togglePlanActivation(plan)
            },
            onDeleteClick = { plan ->
                showDeleteConfirmation(plan)
            }
        )
        binding.recyclerPlans.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddPlan.setOnClickListener {
            showPlanNameDialog(null)
        }
    }

    private fun loadPlans() {
        // Observe plans from database
        database.workoutPlanDao().getAllPlans().observe(viewLifecycleOwner) { plans ->
            lifecycleScope.launch {
                // Get exercise counts for each plan
                val plansWithDetails = mutableListOf<WorkoutPlanWithDetails>()

                for (plan in plans) {
                    val exerciseCount = database.planExerciseDao().getExerciseCountForPlan(plan.id)

                    // Count unique days
                    val exercises = database.planExerciseDao().getExercisesForPlanAndDay(plan.id, 0) // Temp - we'll fix this
                    val dayCount = 0 // TODO: We'll calculate this properly later

                    plansWithDetails.add(
                        WorkoutPlanWithDetails(
                            plan = plan,
                            exerciseCount = exerciseCount,
                            dayCount = dayCount
                        )
                    )
                }

                adapter.submitList(plansWithDetails)

                // Show/hide empty state
                if (plansWithDetails.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.recyclerPlans.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.recyclerPlans.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showPlanNameDialog(existingPlan: WorkoutPlan?) {
        val dialogBinding = DialogPlanNameBinding.inflate(layoutInflater)

        // Pre-fill if editing
        existingPlan?.let {
            dialogBinding.editPlanName.setText(it.name)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (existingPlan == null) "Create Plan" else "Rename Plan")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val name = dialogBinding.editPlanName.text.toString().trim()
                if (name.isEmpty()) {
                    dialogBinding.layoutPlanName.error = "Name is required"
                    return@setOnClickListener
                }

                if (existingPlan == null) {
                    createPlan(name)
                } else {
                    renamePlan(existingPlan, name)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun createPlan(name: String) {
        lifecycleScope.launch {
            val plan = WorkoutPlan(
                id = 0,
                name = name,
                isActive = false
            )
            database.workoutPlanDao().insert(plan)
            Snackbar.make(binding.root, "Plan created", Snackbar.LENGTH_SHORT).show()
            loadPlans()
        }
    }

    private fun renamePlan(plan: WorkoutPlan, newName: String) {
        lifecycleScope.launch {
            database.workoutPlanDao().update(plan.copy(name = newName))
            Snackbar.make(binding.root, "Plan renamed", Snackbar.LENGTH_SHORT).show()
            loadPlans()
        }
    }

    private fun togglePlanActivation(plan: WorkoutPlan) {
        lifecycleScope.launch {
            if (plan.isActive) {
                // Deactivate
                database.workoutPlanDao().update(plan.copy(isActive = false))
                Snackbar.make(binding.root, "Plan deactivated", Snackbar.LENGTH_SHORT).show()
            } else {
                // Activate (deactivates all others)
                database.workoutPlanDao().activatePlan(plan.id)
                Snackbar.make(binding.root, "${plan.name} activated", Snackbar.LENGTH_SHORT).show()
            }
            loadPlans()
        }
    }

    private fun showDeleteConfirmation(plan: WorkoutPlan) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Plan")
            .setMessage("Are you sure you want to delete \"${plan.name}\"? This will also delete all exercises in this plan.")
            .setPositiveButton("Delete") { _, _ ->
                deletePlan(plan)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePlan(plan: WorkoutPlan) {
        lifecycleScope.launch {
            database.workoutPlanDao().delete(plan)
            Snackbar.make(binding.root, "Plan deleted", Snackbar.LENGTH_SHORT).show()
            loadPlans()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}