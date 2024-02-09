package com.netplus.qrenginui.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse
import com.netplus.qrenginui.R
import com.netplus.qrenginui.adapters.TabPagerAdapter
import com.netplus.qrenginui.fragments.cards.fragments.CardsFragment
import com.netplus.qrenginui.fragments.cards.fragments.RecentTokenizedCardFragment
import com.netplus.qrenginui.utils.DataTransferInterface


class CardTokenizationFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var pager: ViewPager
    private lateinit var receiver: BroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_card_tokenization, container, false)
        tabLayout = rootView.findViewById(R.id.cards_tokenization_tab)
        pager = rootView.findViewById(R.id.viewPager)

        initTabs(tabLayout, pager)

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter("swipeAction"))

        return rootView
    }

    private fun initTabs(tab: TabLayout, pager: ViewPager) {
        val tabFragments = ArrayList<Fragment>().apply {
            add(CardsFragment())
            add(RecentTokenizedCardFragment())
        }

        val tabTitles = ArrayList<String>().apply {
            add("Card")
            add("Recent Tokenized Card")
        }

        pager.adapter =
            TabPagerAdapter(childFragmentManager, tabFragments, tabTitles)
        pager.setPadding(0, 0, 0, 0)
        pager.pageMargin = 0
        tab.setupWithViewPager(pager)
        startAutoSwipe()
    }

    private fun startAutoSwipe() {
        receiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "swipeAction") {
                    val generateQrcodeResponse =
                        intent.getSerializableExtra("generateQrcodeResponse") as? GenerateQrcodeResponse
                    val nextItem = (pager.currentItem + 1) % pager.adapter!!.count
                    pager.setCurrentItem(nextItem, true)
                    val fragment =
                        pager.adapter?.instantiateItem(pager, nextItem) as? DataTransferInterface
                    fragment?.transferData(generateQrcodeResponse)
                }
            }
        }
    }
}