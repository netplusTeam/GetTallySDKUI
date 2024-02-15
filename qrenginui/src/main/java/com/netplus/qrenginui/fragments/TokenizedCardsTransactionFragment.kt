package com.netplus.qrenginui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netplus.qrengine.backendRemote.model.transactions.updatedTransaction.UpdatedTransactionResponse
import com.netplus.qrengine.utils.TallSecurityUtil
import com.netplus.qrengine.utils.TallyAppPreferences
import com.netplus.qrengine.utils.TallyQrcodeGenerator
import com.netplus.qrengine.utils.TallyResponseCallback
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.adapters.SingleQrTransactionAdapter
import com.netplus.qrenginui.utils.ProgressDialogUtil

class TokenizedCardsTransactionFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var singleQrTransactionAdapter: SingleQrTransactionAdapter
    private val tallyQrcodeGenerator = TallyQrcodeGenerator()
    private lateinit var qrInfoLayout: LinearLayout
    private val progressDialogUtil by lazy { ProgressDialogUtil(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView =
            inflater.inflate(R.layout.fragment_tokenized_cards_transaction, container, false)

        recyclerView = rootView.findViewById(R.id.all_qr_transaction_recycler)
        qrInfoLayout = rootView.findViewById(R.id.token_info_layout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return rootView
    }

    override fun onResume() {
        super.onResume()
        observer()
    }

    private fun observer() {
        val partnerId = TallyAppPreferences.getInstance(requireContext()).getStringValue(
            TallyAppPreferences.PARTNER_ID)
        val bankName = TallyAppPreferences.getInstance(requireContext()).getStringValue(
            TallyAppPreferences.BANK_NAME)
        val tokenizedCardsData = TallSecurityUtil.retrieveData(requireContext())
        progressDialogUtil.showProgressDialog("Loading...")
        val qr_code_ids = listOf(
            "91b8f53e-0d8c-4118-b3ce-bd75667e5fd1",
            "8a17cd9f-1946-4418-8453-23318d95e01c",
            "c3d11b3f-35d3-4350-82e4-3f47c3c9eee1",
            "8bd94c03-40a8-4087-8f3e-0f45ebb3c279",
            "191a9288-789a-4be6-bb68-c108cabbd1ef",
            "23e9364f-d975-4f06-b453-7d631aac9148",
            "17548a14-4982-4d5f-845e-ed302535bd98",
            "2568390c-4a47-4f44-bb05-2832792df149",
            "b819f729-ef20-440e-9400-6efa792170e8",
            "dcf21547-750e-48f7-af92-a7932504f98e"
        )//extractQrCodeIds(tokenizedCardsData ?: emptyList())
        Log.e("QR", "Data: $qr_code_ids")
        tallyQrcodeGenerator.getTransactions(
            qr_code_ids,
            1,
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
                            null,
                            data?.data?.rows ?: emptyList()
                        )
                        recyclerView.adapter = singleQrTransactionAdapter
                    }

                }

                override fun failed(message: String?) {
                    progressDialogUtil.dismissProgressDialog()
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
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