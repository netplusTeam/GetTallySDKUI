package com.netplus.qrenginui.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.netplus.qrengine.utils.extra
import com.netplus.qrenginui.R
import com.netplus.qrenginui.utils.WebAppInterface

class ChargeCardActivity : AppCompatActivity() {

    private val termUrl by extra<String>("term_url")
    private val md by extra<String>("md")
    private val cReq by extra<String>("c_req")
    private val acsReq by extra<String>("acs_req")
    private val transactionId by extra<String>("transaction_id")
    private val redirectHtml by extra<String>("redirect_html")
    private lateinit var receiver: BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge_card)

        navigateBack()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter("webAction"))

        setToolbar()
        setUpWebView()
    }

    private fun setToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Qr Transaction"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpWebView() {
        findViewById<WebView>(R.id.webview).apply {
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
            }
            addJavascriptInterface(
                WebAppInterface(
                    this@ChargeCardActivity,
                    termUrl = termUrl.toString(),
                    md = md.toString(),
                    cReq = cReq.toString(),
                    ascReq = acsReq.toString(),
                    transactionId = transactionId.toString(),
                    redirectHtml = redirectHtml.toString()
                ),
                "Android"
            )
            loadUrl("file:///android_asset/3ds_pay.html")
        }
    }

    private fun navigateBack() {
        receiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "webAction") {
                    onBackPressed()
                }
            }
        }
    }
}