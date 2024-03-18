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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse
import com.netplus.qrenginui.R
import com.netplus.qrenginui.adapters.TabPagerAdapter
import com.netplus.qrenginui.databinding.CardTokenizationFragmentBinding
import com.netplus.qrenginui.fragments.cards.fragments.CardsFragment
import com.netplus.qrenginui.fragments.cards.fragments.RecentTokenizedCardFragment
import com.netplus.qrenginui.utils.DataTransferInterface


class CardTokenizationFragment : Fragment() {

    private lateinit var binding: CardTokenizationFragmentBinding
    private lateinit var receiver: BroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_card_tokenization, container, false)

        initTabs()

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter("swipeAction"))

        return binding.root
    }

    private fun initTabs() {
        val tabFragments = ArrayList<Fragment>().apply {
            add(CardsFragment())
            add(RecentTokenizedCardFragment())
        }

        val tabTitles = ArrayList<String>().apply {
            add("Card")
            add("Recent Tokenized Card")
        }

        binding.viewPager.apply {
            adapter = TabPagerAdapter(childFragmentManager, tabFragments, tabTitles)
            setPadding(0, 0, 0, 0)
            pageMargin = 0
        }
        binding.cardsTokenizationTab.setupWithViewPager(binding.viewPager)
        startAutoSwipe()
    }

    private fun startAutoSwipe() {
        receiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "swipeAction") {
                    val generateQrcodeResponse =
                        intent.getSerializableExtra("generateQrcodeResponse") as? GenerateQrcodeResponse
                    val nextItem = (binding.viewPager.currentItem + 1) % binding.viewPager.adapter!!.count
                    binding.viewPager.setCurrentItem(nextItem, true)
                    val fragment =
                        binding.viewPager.adapter?.instantiateItem(binding.viewPager, nextItem) as? DataTransferInterface
                    fragment?.transferData(generateQrcodeResponse)
                }
            }
        }
    }
}