package com.netplus.qrenginui.utils

import android.app.Dialog
import android.content.Context
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.netplus.qrenginui.R

class ProgressDialogUtil(private val context: Context) {
    private var progressDialog: Dialog? = null

    fun showProgressDialog(message: String) {
        // Ensure only one instance is running
        if (progressDialog != null) {
            dismissProgressDialog()
        }

        progressDialog = Dialog(context).apply {
            setContentView(R.layout.loading_dialog)
            setCancelable(false) // Make dialog non-cancelable

            val progressText: TextView = findViewById(R.id.progressText)
            progressText.text = message

            val progressImage: ImageView = findViewById(R.id.progressImage)
            val fadeInOutAnimation = AnimationUtils.loadAnimation(context, R.anim.loading_animation)
            progressImage.startAnimation(fadeInOutAnimation)

            show()
        }
    }

    fun dismissProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            progressDialog = null
        }
    }
}