package com.ko.wellness.ui.workouts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ko.wellness.data.entities.ExerciseTemplate
import com.ko.wellness.databinding.ItemExercisePickerBinding

class ExercisePickerAdapter (
    private val onExerciseClick: (ExerciseTemplate) -> Unit
) : ListAdapter<ExerciseTemplate, ExercisePickerAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExercisePickerBinding.inflate(
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
        private val binding: ItemExercisePickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(template: ExerciseTemplate) {
            binding.textExerciseName.text = template.name
            binding.textCategory.text = template.category

            // Show default sets/reps or sets/duration
            val typeInfo = when (template.exerciseType) {
                "REPS" -> "${template.defaultSets} sets × ${template.defaultReps} reps"
                "TIMED" -> "${template.defaultSets} sets × ${template.defaultDuration}s"
                else -> ""
            }
            binding.textExerciseType.text = typeInfo

            binding.root.setOnClickListener {
                onExerciseClick(template)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ExerciseTemplate>() {
        override fun areItemsTheSame(
            oldItem: ExerciseTemplate,
            newItem: ExerciseTemplate
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ExerciseTemplate,
            newItem: ExerciseTemplate
        ): Boolean {
            return oldItem == newItem
        }
    }
}