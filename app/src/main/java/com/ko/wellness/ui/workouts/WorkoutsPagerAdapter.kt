package com.ko.wellness.ui.workouts

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WorkoutsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2  // Two tabs: Today and Plans

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TodayFragment()
            1 -> PlansFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}