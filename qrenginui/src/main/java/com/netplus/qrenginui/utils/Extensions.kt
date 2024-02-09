package com.netplus.qrenginui.utils

import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Launches an Activity from the Fragment and passes a String extra.
 *
 * @param T The Activity class to be launched.
 * @param key The key for the intent extra.
 * @param data The string data to pass to the activity.
 */
inline fun <reified T: Any> Fragment.launchActivityWithExtra(key: String, data: String) {
    val intent = Intent(activity, T::class.java).apply {
        putExtra(key, data)
    }
    startActivity(intent)
}