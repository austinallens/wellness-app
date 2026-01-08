package com.ko.wellness.ui.workouts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ko.wellness.data.entities.WorkoutPlan
import com.ko.wellness.databinding.ItemWorkoutPlanBinding

data class WorkoutPlanWithDetails(
    val plan: WorkoutPlan,
    val exerciseCount: Int,
    val dayCount: Int
)

class WorkoutPlanAdapter(
    private val onEditClick: (WorkoutPlan) -> Unit,
    private val onActivateClick: (WorkoutPlan) -> Unit,
    private val onDeleteClick: (WorkoutPlan) -> Unit
) : ListAdapter<WorkoutPlanWithDetails, WorkoutPlanAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemWorkoutPlanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(planWithDetails: WorkoutPlanWithDetails) {
            val plan = planWithDetails.plan

            binding.textPlanName.text = plan.name

            // Show / Hide Active Chip
            if (plan.isActive) {
                binding.chipActive.visibility = View.VISIBLE
                binding.buttonActivate.text = "Deactivate"
            } else {
                binding.chipActive.visibility = View.GONE
                binding.buttonActivate.text = "Activate"
            }

            // Show Exercise Count
            val exerciseText = if (planWithDetails.dayCount > 0) {
                "${planWithDetails.exerciseCount} exercises across ${planWithDetails.dayCount} days"
            } else {
                "No exercises added yet"
            }
            binding.textExerciseCount.text = exerciseText

            // Click Handlers
            binding.buttonEdit.setOnClickListener {
                onEditClick(plan)
            }

            binding.buttonActivate.setOnClickListener {
                onActivateClick(plan)
            }

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(plan)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<WorkoutPlanWithDetails>() {
        override fun areItemsTheSame(
            oldItem: WorkoutPlanWithDetails,
            newItem: WorkoutPlanWithDetails
        ): Boolean {
            return oldItem.plan.id == newItem.plan.id
        }

        override fun areContentsTheSame(
            oldItem: WorkoutPlanWithDetails,
            newItem: WorkoutPlanWithDetails
        ): Boolean {
            return oldItem == newItem
        }
    }
}