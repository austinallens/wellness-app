package com.ko.wellness.ui.workouts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ko.wellness.data.entities.ExerciseTemplate
import com.ko.wellness.data.entities.ExerciseType
import com.ko.wellness.databinding.ItemExerciseTemplateBinding

class ExerciseTemplateAdapter (
    private val onDeleteClick: (ExerciseTemplate) -> Unit,
    private val onItemClick: (ExerciseTemplate) -> Unit
) : ListAdapter<ExerciseTemplate, ExerciseTemplateAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExerciseTemplateBinding.inflate(
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
        private val binding: ItemExerciseTemplateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(template: ExerciseTemplate) {
            binding.textExerciseName.text = template.name
            binding.textCategory.text = template.category

            // Format Exercise Type Info
            val typeInfo = when (ExerciseType.fromString(template.exerciseType)) {
                ExerciseType.REPS -> {
                    "${template.defaultSets} sets × ${template.defaultReps} reps"
                }
                ExerciseType.TIMED -> {
                    "${template.defaultSets} sets × ${template.defaultDuration}s"
                }
            }
            binding.textExerciseType.text = typeInfo

            // Click Handlers
            binding.buttonDelete.setOnClickListener {
                onDeleteClick(template)
            }

            binding.root.setOnClickListener {
                onItemClick(template)
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