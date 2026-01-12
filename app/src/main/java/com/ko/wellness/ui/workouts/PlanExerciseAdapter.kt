package com.ko.wellness.ui.workouts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ko.wellness.databinding.ItemPlanCategoryHeaderBinding
import com.ko.wellness.databinding.ItemPlanExerciseBinding
import com.ko.wellness.ui.workouts.PlanBuilderItem

class PlanExerciseAdapter(
    private val onDeleteClick: (PlanBuilderItem.Exercise) -> Unit,
    private val onMoveItem: (fromPosition: Int, toPostition: Int) -> Unit
) : ListAdapter<PlanBuilderItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_EXERCISE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PlanBuilderItem.Header -> TYPE_HEADER
            is PlanBuilderItem.Exercise -> TYPE_EXERCISE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemPlanCategoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            TYPE_EXERCISE -> {
                val binding = ItemPlanExerciseBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ExerciseViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PlanBuilderItem.Header -> (holder as HeaderViewHolder).bind(item)
            is PlanBuilderItem.Exercise -> (holder as ExerciseViewHolder).bind(item)
        }
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val fromItem = getItem(fromPosition)
        val toItem = getItem(toPosition)

        // Only allow moving exercises, not headers
        if (fromItem is PlanBuilderItem.Exercise && toItem is PlanBuilderItem.Exercise) {
            // Only allow moving within same category
            if (fromItem.template.category == toItem.template.category) {
                onMoveItem(fromPosition, toPosition)
            }
        }
    }

    inner class HeaderViewHolder(
        private val binding: ItemPlanCategoryHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: PlanBuilderItem.Header) {
            binding.textCategoryName.text = header.category
        }
    }

    inner class ExerciseViewHolder(
        private val binding: ItemPlanExerciseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlanBuilderItem.Exercise) {
            val template = item.template
            val planExercise = item.planExercise

            binding.textExerciseName.text = template.name
            binding.textCategory.text = template.category

            // Show sets/reps or sets/duration
            val details = if (planExercise.reps != null) {
                "${planExercise.sets} sets × ${planExercise.reps} reps"
            } else {
                "${planExercise.sets} sets × ${planExercise.duration}s"
            }
            binding.textExerciseDetails.text = details

            // Delete button
            binding.buttonDelete.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PlanBuilderItem>() {
        override fun areItemsTheSame(
            oldItem: PlanBuilderItem,
            newItem: PlanBuilderItem
        ): Boolean {
            return when {
                oldItem is PlanBuilderItem.Header && newItem is PlanBuilderItem.Header ->
                    oldItem.category == newItem.category
                oldItem is PlanBuilderItem.Exercise && newItem is PlanBuilderItem.Exercise ->
                    oldItem.planExercise.id == newItem.planExercise.id
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: PlanBuilderItem,
            newItem: PlanBuilderItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}
