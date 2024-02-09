package com.netplus.qrenginui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.netplus.qrengine.backendRemote.model.login.LoginResponse
import com.netplus.qrengine.utils.TallyQrcodeGenerator
import com.netplus.qrengine.utils.TallyResponseCallback
import com.netplus.qrenginui.R
import com.netplus.qrenginui.utils.ProgressDialogUtil
import com.netplus.qrenginui.adapters.TabPagerAdapter
import com.netplus.qrenginui.fragments.AllTokenizedCardsFragment
import com.netplus.qrenginui.fragments.CardTokenizationFragment
import com.netplus.qrenginui.fragments.TallyMerchantsFragment
import com.netplus.qrenginui.fragments.TokenizedCardsTransactionFragment

class TallyActivity : AppCompatActivity() {

    private val tallyQrcodeGenerator = TallyQrcodeGenerator()
    lateinit var tabPager: ViewPager
    lateinit var tabLayout: TabLayout
    private val progressDialogUtil by lazy { ProgressDialogUtil(this) }

    companion object {
        private const val EXTRA_EMAIL = "extra_email"
        private const val EXTRA_PASSWORD = "extra_password"

        fun getIntent(context: Context, email: String, password: String): Intent {
            return Intent(context, TallyActivity::class.java).apply {
                putExtra(EXTRA_EMAIL, email)
                putExtra(EXTRA_PASSWORD, password)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tally)

        val email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        val password = intent.getStringExtra(EXTRA_PASSWORD) ?: ""
        authenticateBank(email, password)

        tabPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tally_tab)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        initTabs(tabLayout, tabPager)
    }

    private fun initTabs(tabLayout: TabLayout, tabPager: ViewPager) {

        val tabFragments = ArrayList<Fragment>().apply {
            add(CardTokenizationFragment())
            add(AllTokenizedCardsFragment())
            add(TokenizedCardsTransactionFragment())
            add(TallyMerchantsFragment())
        }

        val tabTitles = ArrayList<String>().apply {
            add("Cards")
            add("Tokenized Cards")
            add("All Transactions")
            add("All Merchants")
        }

        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        tabPager.adapter = TabPagerAdapter(supportFragmentManager, tabFragments, tabTitles)
        tabPager.setPadding(0, 0, 0, 0)
        tabPager.pageMargin = 0
        tabLayout.setupWithViewPager(tabPager)
    }

    private fun authenticateBank(
        email: String,
        password: String
    ) {

        progressDialogUtil.showProgressDialog("Authenticating...")
        tallyQrcodeGenerator.authenticateBank(
            email = email,
            password = password,
            object : TallyResponseCallback<LoginResponse> {
                override fun success(data: LoginResponse?) {
                    Toast.makeText(
                        this@TallyActivity,
                        "Authentication Successful",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialogUtil.dismissProgressDialog()
                }

                override fun failed(message: String?) {
                    Toast.makeText(this@TallyActivity, message.toString(), Toast.LENGTH_SHORT)
                        .show()
                    progressDialogUtil.dismissProgressDialog()
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        progressDialogUtil.dismissProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialogUtil.dismissProgressDialog()
    }
}