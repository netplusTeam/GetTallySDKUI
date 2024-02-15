package com.netplus.qrenginui.activities

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netplus.qrengine.backendRemote.model.transactions.updatedTransaction.UpdatedTransactionResponse
import com.netplus.qrengine.utils.TallyQrcodeGenerator
import com.netplus.qrengine.utils.TallyResponseCallback
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.adapters.SingleQrTransactionAdapter
import com.netplus.qrenginui.utils.ProgressDialogUtil

class SingleQrTransactionsActivity : AppCompatActivity(), SingleQrTransactionAdapter.Interaction {

    private lateinit var recyclerView: RecyclerView
    private lateinit var singleQrTransactionAdapter: SingleQrTransactionAdapter
    private val tallyQrcodeGenerator = TallyQrcodeGenerator()
    private var qrcodeId = ""
    private lateinit var qrInfoLayout: LinearLayout
    private val progressDialogUtil by lazy { ProgressDialogUtil(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_qr_transactions)

        qrcodeId = intent.getStringExtra("qrcode_id").toString()
        recyclerView = findViewById(R.id.single_qr_transaction_recycler)
        qrInfoLayout = findViewById(R.id.token_info_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Qr Transaction"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        observer()
    }

    private fun observer() {
        progressDialogUtil.showProgressDialog("Loading...")
        //TODO: use [qrcodeId]
        val qr_code_ids = listOf("8a17cd9f-1946-4418-8453-23318d95e01c")
        tallyQrcodeGenerator.getTransactions(
            qr_code_ids,
            2,
            10,
            object : TallyResponseCallback<UpdatedTransactionResponse> {
                override fun success(data: UpdatedTransactionResponse?) {
                    progressDialogUtil.dismissProgressDialog()
                    Log.e("TAG", "success: $data")
                    if (data?.data?.rows.isNullOrEmpty()) {
                        switchViewVisibility(true)
                    } else {
                        switchViewVisibility(false)
                        singleQrTransactionAdapter = SingleQrTransactionAdapter(
                            this@SingleQrTransactionsActivity,
                            data?.data?.rows ?: emptyList()
                        )
                        recyclerView.adapter = singleQrTransactionAdapter
                    }
                }

                override fun failed(message: String?) {
                    progressDialogUtil.dismissProgressDialog()
                    Toast.makeText(this@SingleQrTransactionsActivity, message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    override fun onItemSelected() {

    }

    private fun switchViewVisibility(isListEmpty: Boolean) {
        if (isListEmpty) {
            recyclerView.gone()
            qrInfoLayout.visible()
        } else {
            recyclerView.visible()
            qrInfoLayout.gone()
        }
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