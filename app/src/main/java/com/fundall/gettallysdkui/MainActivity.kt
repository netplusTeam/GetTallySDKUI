package com.fundall.gettallysdkui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netplus.qrenginui.activities.TallyActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val email = "nicholasanyanwu125@gmail.com"
        val password = "nicholasanyanwu125@gmail.com"
        val fullName = "Anyanwu Nicholas"
        val bankName = "Wema"
        val phoneNumber = "09090909090"
        val userId = "28"
        val intent = TallyActivity.getIntent(this, email, password, fullName, bankName, phoneNumber, userId)
        startActivity(intent)
    }
}