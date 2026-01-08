package com.ko.wellness

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ko.wellness.databinding.ActivityMainBinding
import com.ko.wellness.ui.calendar.CalendarFragment
import com.ko.wellness.ui.home.HomeFragment
import com.ko.wellness.ui.mealplan.MealPlanFragment
import com.ko.wellness.ui.settings.SettingsFragment
import com.ko.wellness.ui.workouts.TodayFragment
import com.ko.wellness.ui.workouts.WorkoutsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Up ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the WorkoutFragments on app start (only once)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Set Up Bottom Navigation Listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.navigation_meal_plan -> {
                    loadFragment(MealPlanFragment())
                    true
                }

                R.id.navigation_workouts -> {
                    loadFragment(WorkoutsFragment())
                    true
                }

                R.id.navigation_calendar -> {
                    loadFragment(CalendarFragment())
                    true
                }

                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}