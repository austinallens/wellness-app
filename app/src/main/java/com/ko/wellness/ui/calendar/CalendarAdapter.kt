package com.ko.wellness.ui.calendar

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ko.wellness.R
import com.ko.wellness.databinding.ItemCalendarDayBinding

class CalendarAdapter (
    private val onDayClicked: (CalendarDay) -> Unit
): ListAdapter<CalendarDay, CalendarAdapter.DayViewHolder>(CalendarDayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DayViewHolder(binding, onDayClicked)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(
        private val binding: ItemCalendarDayBinding,
        private val onDayClicked: (CalendarDay) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: CalendarDay) {
            // Set Day Number
            binding.tvDayNumber.text = if (day.isCurrentMonth) {
                day.dayOfMonth.toString()
            } else {
                "" // Don't Show Days from other Months
            }

            // Set Text Color Based on Whether it's Current Month
            binding.tvDayNumber.alpha = if (day.isCurrentMonth) 1.0f else 0.3f

            // Get the Background Drawable and set its Color
            val background = binding.dayBackground.background as? GradientDrawable

            if (day.isCurrentMonth) {
                    // Set Color based on Completion Status
                val color = when (day.completionStatus) {
                    CompletionStatus.COMPLETE -> 0xFF4CAF50.toInt()     // Green
                    CompletionStatus.PARTIAL -> 0xFFFF9800.toInt()      // Orange
                    CompletionStatus.NOT_STARTED -> 0xFFF44336.toInt()  // Red
                    CompletionStatus.NONE -> 0xFF2C2C2C.toInt()         // Dark gray
                }
                background?.setColor(color)

                // Highlight Today with a Border
                if (day.isToday) {
                    background?.setStroke(3, 0xFFBB86FC.toInt())  // Purple border
                } else {
                    background?.setStroke(0, 0)  // No border
                }
            } else {
                // Days from other months - very dark
                background?.setColor(0xFF1E1E1E.toInt())
                background?.setStroke(0, 0)
            }

            // Click listener
            binding.root.setOnClickListener {
                if (day.isCurrentMonth) {
                    onDayClicked(day)
                }
            }
        }
    }

    class CalendarDayDiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem == newItem
        }
    }
}
