package com.netplus.qrenginui.utils

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.netplus.qrengine.backendRemote.model.card.WebResponse
import com.netplus.qrengine.utils.TallyAppPreferences

class WebAppInterface(
    private val mContext: Context,
    private val termUrl: String,
    private val md: String,
    private val cReq: String,
    private val ascReq: String,
    private val transactionId: String,
    private val redirectHtml: String
) {

    private val webViewBaseUrl = "https://api.netpluspay.com/transactions/requery/MID63dbdc67badab/"

    // Function to be called from JavaScript
    @JavascriptInterface
    fun sendValueToWebView(): String {
        return "$termUrl<======>$md<======>$cReq<======>$ascReq<======>$transactionId<======>$webViewBaseUrl<======>$redirectHtml"
    }

    // Callback function to receive data from WebView
    @JavascriptInterface
    fun webViewCallback(data: String) {
        // Handle the callback data
        if (data.isNotEmpty()) {
            val mappedWebResponse = Gson().fromJson(data, WebResponse::class.java)
            TallyAppPreferences.getInstance(mContext)
                .setStringValue(TallyAppPreferences.WEB_VIEW_CODE, mappedWebResponse.code)
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent("webAction").apply {
                    putExtra("code", mappedWebResponse.code)
                }
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
            }, 1000)
            Log.e("RETURNED_DATA", "webViewCallback: $data")
        }
    }
}
