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
import com.netplus.qrengine.backendRemote.model.keys.FinancialInstitutionKeyResponse
import com.netplus.qrengine.backendRemote.model.keys.FinancialInstitutionPayload
import com.netplus.qrengine.backendRemote.model.login.LoginResponse
import com.netplus.qrengine.utils.STORE_PARTNER_KEYS
import com.netplus.qrengine.utils.TOKEN
import com.netplus.qrengine.utils.TallyAppPreferences
import com.netplus.qrengine.utils.TallyQrcodeGenerator
import com.netplus.qrengine.utils.TallyResponseCallback
import com.netplus.qrengine.utils.showSnackbar
import com.netplus.qrenginui.R
import com.netplus.qrenginui.adapters.TabPagerAdapter
import com.netplus.qrenginui.fragments.AllTokenizedCardsFragment
import com.netplus.qrenginui.fragments.CardTokenizationFragment
import com.netplus.qrenginui.fragments.TallyMerchantsFragment
import com.netplus.qrenginui.fragments.TokenizedCardsTransactionFragment
import com.netplus.qrenginui.utils.ProgressDialogUtil
import com.netplus.qrenginui.utils.generateRandomString

class TallyActivity : AppCompatActivity() {

    private val tallyQrcodeGenerator = TallyQrcodeGenerator()
    private lateinit var tabPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private val progressDialogUtil by lazy { ProgressDialogUtil(this) }

    companion object {
        private const val EXTRA_EMAIL = "extra_email"
        //private const val EXTRA_PASSWORD = "extra_password"
        private const val EXTRA_FULL_NAME = "extra_full_name"
        private const val EXTRA_BANK_NAME = "extra_bank_name"
        private const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        private const val EXTRA_USER_ID = "extra_user_id"

        fun getIntent(
            context: Context,
            email: String,
            //password: String?,
            fullName: String,
            bankName: String?,
            phoneNumber: String?,
            userId: String?
        ): Intent {
            return Intent(context, TallyActivity::class.java).apply {
                putExtra(EXTRA_EMAIL, email)
                //putExtra(EXTRA_PASSWORD, password)
                putExtra(EXTRA_FULL_NAME, fullName)
                putExtra(EXTRA_BANK_NAME, bankName)
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(EXTRA_USER_ID, userId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tally)

        val email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        //val password = intent.getStringExtra(EXTRA_PASSWORD) ?: ""
        val fullName = intent.getStringExtra(EXTRA_FULL_NAME) ?: ""
        val bankName = intent.getStringExtra(EXTRA_BANK_NAME) ?: ""
        val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
        TallyAppPreferences.getInstance(this).apply {
            setStringValue(TallyAppPreferences.FULL_NAME, fullName)
            setStringValue(TallyAppPreferences.BANK_NAME, bankName)
            setStringValue(TallyAppPreferences.PHONE_NUMBER, phoneNumber)
            setStringValue(TallyAppPreferences.USER_ID, userId)
            setStringValue(TallyAppPreferences.EMAIL, email)
        }
        //authenticateBank(email, password)
        val isFirstTimeLaunch = TallyAppPreferences.getInstance(this)
            .getBooleanValue(TallyAppPreferences.IS_APP_FIRST_LAUNCHED)
        if (!isFirstTimeLaunch) {
            //storePartnerInfo(bankName)
        }

        tabPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tally_tab)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Tally"

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
            add("Transactions")
            add("Merchants")
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
                    showSnackbar(message = "Authentication Successful")
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

    private fun storePartnerInfo(bankName: String) {
        progressDialogUtil.showProgressDialog("Loading...")
        val partnerId = "$bankName ${generateRandomString(10)}"
        val financialInstitutionPayload = FinancialInstitutionPayload(
            partner_name = bankName,
            secret = partnerId
        )
        tallyQrcodeGenerator.storeFinancialInstitutionKeys(
            STORE_PARTNER_KEYS,
            TOKEN,
            financialInstitutionPayload,
            object : TallyResponseCallback<FinancialInstitutionKeyResponse> {
                override fun failed(message: String?) {
                    progressDialogUtil.dismissProgressDialog()
                    Toast.makeText(this@TallyActivity, message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

                override fun success(data: FinancialInstitutionKeyResponse?) {
                    TallyAppPreferences.getInstance(this@TallyActivity)
                        .setBooleanValue(TallyAppPreferences.IS_APP_FIRST_LAUNCHED, true)
                    progressDialogUtil.dismissProgressDialog()
                    showSnackbar(message = "Setup successful")
                    TallyAppPreferences.getInstance(this@TallyActivity)
                        .setStringValue(TallyAppPreferences.PARTNER_ID, partnerId)
                }
            })
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