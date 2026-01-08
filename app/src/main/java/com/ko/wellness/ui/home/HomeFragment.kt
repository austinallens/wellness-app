package com.ko.wellness.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ko.wellness.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // For now, just show a simple text view
        return TextView(requireContext()).apply {
            text = "Home Dashboard\n\nComing Soon!"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(32, 100, 32, 32)
            setBackgroundColor(0xFF121212.toInt())
        }
    }
}