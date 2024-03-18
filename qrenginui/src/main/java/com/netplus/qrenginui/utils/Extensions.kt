package com.netplus.qrenginui.utils

import android.content.Intent
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrPayload
import com.netplus.qrenginui.R
import java.util.UUID

/**
 * Launches an Activity from the Fragment and passes a String extra.
 *
 * @param T The Activity class to be launched.
 * @param key The key for the intent extra.
 * @param data The string data to pass to the activity.
 */
inline fun <reified T : Any> Fragment.launchActivityWithExtra(key: String, data: String) {
    val intent = Intent(activity, T::class.java).apply {
        putExtra(key, data)
    }
    startActivity(intent)
}

fun generateRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

val cardSchemeLogos = listOf(R.drawable.mastercard_logo, R.drawable.verve_logo, R.drawable.visa)

fun generateOrderId() = UUID.randomUUID().toString().replace("-", "")

fun createClientDataForNonVerveCard(
    transID: String, cardNumber: String, expiryDate: String, cvv: String
): String = "$transID:LIVE:$cardNumber:$expiryDate:$cvv::NGN:QR"

fun createClientDataForVerveCard(
    generateQrPayload: GenerateQrPayload, transID: String
): String {
    val verve = generateQrPayload.let {
        "$transID:LIVE:${it.card_number}:${it.card_expiry}:${it.card_cvv}:${generateQrPayload.card_pin}:NGN:QR"
    }
    Log.e("VERVE_TAG", "createClientDataForVerveCard: $verve")
    return verve
}

fun stringToBase64(text: String): String {
    val data: ByteArray = text.toByteArray()
    return Base64.encodeToString(data, Base64.DEFAULT)
}

fun base64ToPlainText(base64String: String): String {
    val decodedString = Base64.decode(base64String, Base64.DEFAULT)
    return String(decodedString)
}