package com.netplus.qrenginui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TabPagerAdapter(
    fm: FragmentManager,
    private var fragmentList: ArrayList<Fragment>,
    private var pageNames: ArrayList<String>
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return pageNames[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}