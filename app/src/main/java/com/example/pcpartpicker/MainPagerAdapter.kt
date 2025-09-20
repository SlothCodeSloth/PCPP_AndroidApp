package com.example.pcpartpicker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Pager adapter for [MainActivity].
 *
 * Handles switching between the following:
 * - [SettingsFragment]
 * - [MainSearchFragment]
 * - [ListOverviewFragment]
 */
class MainPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val listNames: List<String>
)  : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SettingsFragment()
            1 -> MainSearchFragment()
            2 -> ListOverviewFragment()
            else -> throw IndexOutOfBoundsException("Invalid Index")
        }
    }
}