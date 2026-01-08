package com.ko.wellness.ui.workouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.ko.wellness.databinding.FragmentWorkoutsBinding

class WorkoutsFragment : Fragment() {

    private var _binding: FragmentWorkoutsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DEBUG: Log to verify this fragment is loading
        android.util.Log.d("WorkoutsFragment", "onViewCreated called!")
        android.util.Log.d("WorkoutsFragment", "TabLayout: ${binding.tabLayout}")
        android.util.Log.d("WorkoutsFragment", "ViewPager: ${binding.viewPager}")

        // Set up ViewPager with adapter
        val adapter = WorkoutsPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Connect TabLayout with ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Today"
                1 -> "Plans"
                else -> null
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}