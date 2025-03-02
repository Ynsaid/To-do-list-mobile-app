package com.example.todolist


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val completedFragment = CompletedFragment()
    private val pendingFragment = PendingFragment()
    private val overdueFragment = OverdueFragment()

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> completedFragment
            1 -> pendingFragment
            2 -> overdueFragment
            else -> pendingFragment
        }
    }

    override fun getCount(): Int {
        return 3
    }

    fun getPendingFragment(): PendingFragment {
        return pendingFragment
    }
}
