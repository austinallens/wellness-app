package com.ko.wellness.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ko.wellness.data.database.AppDatabase
import com.ko.wellness.databinding.FragmentCalendarBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var adapter: CalendarAdapter

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        // Set Up Adapter
        adapter = CalendarAdapter { day ->
            // TODO: Later we'll open a dialog showing that day's workouts
            // For now, just a simple toast or log
        }
        binding.calendarRecyclerView.adapter = adapter

        // Set Up Navigation Buttons
        binding.btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        // Load Current Month
        updateCalendar()
    }

    private fun updateCalendar() {
        // Update Month / Year Text
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonthYear.text = monthYearFormat.format(calendar.time)

        // Generate Calendar Days
        val days = generateCalendarDays()

        // Load Workout Completion Data for this Month
        lifecycleScope.launch {
            val daysWithStatus = loadWorkoutCompletionStatus(days)
            adapter.submitList(daysWithStatus)
        }
    }

    private fun generateCalendarDays(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()

        // Get First Day of the Month
        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)

        // Get last day of the month
        val lastDayOfMonth = calendar.clone() as Calendar
        lastDayOfMonth.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        // Get today for comparison
        val today = Calendar.getInstance()

        // Find what day of week the month starts on (0 = Sunday, 6 = Saturday)
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1

        // Add empty cells for days before the month starts
        val prevMonth = calendar.clone() as Calendar
        prevMonth.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 0 until firstDayOfWeek) {
            val dayNum = daysInPrevMonth - firstDayOfWeek + i + 1
            val date = prevMonth.clone() as Calendar
            date.set(Calendar.DAY_OF_MONTH, dayNum)

            days.add(
                CalendarDay(
                    date = date.time,
                    dayOfMonth = dayNum,
                    isCurrentMonth = false,
                    isToday = false,
                    completionStatus = CompletionStatus.NONE
                )
            )
        }

        // Add All Days of Current Month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (dayNum in 1..daysInMonth) {
            val date = calendar.clone() as Calendar
            date.set(Calendar.DAY_OF_MONTH, dayNum)

            val isToday = date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            days.add(CalendarDay(
                date = date.time,
                dayOfMonth = dayNum,
                isCurrentMonth = true,
                isToday = isToday,
                completionStatus = CompletionStatus.NONE  // Will be updated with real data
            ))
        }

        // Add days from next month to fill the grid (up to 42 total cells = 6 weeks)
        val nextMonth = calendar.clone() as Calendar
        nextMonth.add(Calendar.MONTH, 1)

        var dayNum = 1
        while (days.size < 42) {
            val date = nextMonth.clone() as Calendar
            date.set(Calendar.DAY_OF_MONTH, dayNum)

            days.add(CalendarDay(
                date = date.time,
                dayOfMonth = dayNum,
                isCurrentMonth = false,
                isToday = false,
                completionStatus = CompletionStatus.NONE
            ))
            dayNum++
        }

        return days
    }

    private suspend fun loadWorkoutCompletionStatus(days: List<CalendarDay>): List<CalendarDay> {
        // For each day, query the database to get completion status
        return days.map { day ->
            if (day.isCurrentMonth) {
                val dateString = dateFormat.format(day.date)
                val totalCount = database.workoutExerciseDao().getWorkoutCountForDate(dateString)
                val completedCount = database.workoutExerciseDao().getCompletedCountForDate(dateString)

                val status = when {
                    totalCount == 0 -> CompletionStatus.NONE
                    completedCount == 0 -> CompletionStatus.NOT_STARTED
                    completedCount == totalCount -> CompletionStatus.COMPLETE
                    else -> CompletionStatus.PARTIAL
                }

                day.copy(completionStatus = status)
            } else {
                day
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}