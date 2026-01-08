package com.ko.wellness.ui.workouts

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PlansPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ExerciseLibraryFragment()
            1 -> PlansListFragment()
            else -> ExerciseLibraryFragment()
        }
    }
}