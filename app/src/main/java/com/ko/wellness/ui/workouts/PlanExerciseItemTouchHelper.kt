package com.ko.wellness.ui.workouts

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class PlanExerciseItemTouchHelper(
    private val adapter: PlanExerciseAdapter
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // Only allow dragging exercises, not headers
        val dragFlags = if (viewHolder is PlanExerciseAdapter.ExerciseViewHolder) {
            ItemTouchHelper.UP or ItemTouchHelper.DOWN
        } else {
            0
        }
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition

        adapter.onItemMove(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not using swipe
    }

    override fun isLongPressDragEnabled(): Boolean = true
}