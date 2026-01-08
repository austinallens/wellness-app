package com.ko.wellness.ui.workouts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ko.wellness.data.entities.WorkoutExercise
import com.ko.wellness.databinding.ItemCategoryHeaderBinding
import com.ko.wellness.databinding.ItemWorkoutExerciseBinding

class WorkoutAdapter(
    private val onCheckboxClicked: (Long, Boolean) -> Unit
) : ListAdapter<WorkoutItem, RecyclerView.ViewHolder>(WorkoutDiffCallback()) {
    // View Type Constants
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_EXERCISE = 1
    }

    // Determine which View Type for each Position
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is WorkoutItem.Header -> VIEW_TYPE_HEADER
            is WorkoutItem.Exercise -> VIEW_TYPE_EXERCISE
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    // Create the Appropriate ViewHolder based on view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemCategoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_EXERCISE -> {
                val binding = ItemWorkoutExerciseBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ExerciseViewHolder(binding, onCheckboxClicked)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    // Bind Data to the ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is WorkoutItem.Header -> (holder as HeaderViewHolder).bind(item.category)
            is WorkoutItem.Exercise -> (holder as ExerciseViewHolder).bind(item.exercise)
        }
    }

    // ViewHolder for Category Headers
    class HeaderViewHolder(
        private val binding: ItemCategoryHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String) {
            binding.tvCategoryHeader.text = category.uppercase()
        }
    }

    // ViewHolder for Exercise Items
    class ExerciseViewHolder(
        private val binding: ItemWorkoutExerciseBinding,
        private val onCheckboxClicked: (Long, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: WorkoutExercise) {
            binding.tvExerciseName.text = exercise.name

            // Format the Details Text Based on Exercise Type
            binding.tvExerciseDetails.text = when {
                exercise.reps != null -> {
                    "${exercise.sets} sets × ${exercise.reps} reps"
                }
                exercise.duration != null -> {
                    "${exercise.sets} sets × ${exercise.duration}s"
                }
                else -> {
                    "${exercise.sets} sets"
                }
            }

            // Set Checkbox State without triggering the Listener
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = exercise.isCompleted

            // Set Up Checkbox Click Listener
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckboxClicked(exercise.id, isChecked)
            }
        }
    }

    // DiffUtil for Efficient List Updates
    class WorkoutDiffCallback : DiffUtil.ItemCallback<WorkoutItem>() {
        override fun areItemsTheSame(oldItem: WorkoutItem, newItem: WorkoutItem): Boolean {
            return when {
                oldItem is WorkoutItem.Header && newItem is WorkoutItem.Header ->
                    oldItem.category == newItem.category
                oldItem is WorkoutItem.Exercise && newItem is WorkoutItem.Exercise ->
                    oldItem.exercise.id == newItem.exercise.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: WorkoutItem, newItem: WorkoutItem): Boolean {
            return oldItem == newItem
        }
    }
}