package com.ko.wellness.ui.calendar

import java.util.*

data class CalendarDay (
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val completionStatus: CompletionStatus
)

enum class CompletionStatus {
    NONE, // No Workouts (Gray)
    NOT_STARTED, // No Workouts Done (Red)
    PARTIAL, // Some Workouts (Orange)
    COMPLETE // All Workouts (Green)
}