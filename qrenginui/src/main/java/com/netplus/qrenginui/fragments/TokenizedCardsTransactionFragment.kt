package com.netplus.qrenginui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.netplus.qrengine.backendRemote.model.transactions.updatedTransaction.UpdatedTransactionResponse
import com.netplus.qrengine.utils.TallSecurityUtil
import com.netplus.qrengine.utils.TallyQrcodeGenerator
import com.netplus.qrengine.utils.TallyResponseCallback
import com.netplus.qrengine.utils.extractQrCodeIds
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.showSnackbar
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.adapters.SingleQrTransactionAdapter
import com.netplus.qrenginui.databinding.TokenizedCardsBinding
import com.netplus.qrenginui.utils.ProgressDialogUtil

class TokenizedCardsTransactionFragment : Fragment() {

    private lateinit var binding: TokenizedCardsBinding
    private lateinit var singleQrTransactionAdapter: SingleQrTransactionAdapter
    private val tallyQrcodeGenerator = TallyQrcodeGenerator()
    private val progressDialogUtil by lazy { ProgressDialogUtil(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater,R.layout.fragment_tokenized_cards_transaction, container, false)

        binding.allQrTransactionRecycler.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        observer()
    }

    private fun observer() {
        /*val partnerId = TallyAppPreferences.getInstance(requireContext()).getStringValue(
            TallyAppPreferences.PARTNER_ID)
        val bankName = TallyAppPreferences.getInstance(requireContext()).getStringValue(
            TallyAppPreferences.BANK_NAME)*/

        val tokenizedCardsData = TallSecurityUtil.retrieveData(requireContext())
        progressDialogUtil.showProgressDialog("Loading...")
        val qr_code_ids = extractQrCodeIds(tokenizedCardsData ?: emptyList())
        Log.e("QR", "Data: $qr_code_ids")
        tallyQrcodeGenerator.getTransactions(
            qr_code_ids,
            1,
            10,
            object : TallyResponseCallback<UpdatedTransactionResponse> {
                override fun success(data: UpdatedTransactionResponse?) {
                    progressDialogUtil.dismissProgressDialog()
                    if (data?.data?.rows.isNullOrEmpty()) {
                        switchViewVisibility(true)
                    } else {
                        switchViewVisibility(false)
                        singleQrTransactionAdapter = SingleQrTransactionAdapter(
                            null,
                            data?.data?.rows ?: emptyList()
                        )
                        binding.allQrTransactionRecycler.adapter = singleQrTransactionAdapter
                    }

                }

                override fun failed(message: String?) {
                    progressDialogUtil.dismissProgressDialog()
                    switchViewVisibility(true)
                    requireContext().showSnackbar(message = "An error occurred")
                }
            })
    }

    private fun switchViewVisibility(isListEmpty: Boolean) {
        if (isListEmpty) {
            binding.allQrTransactionRecycler.gone()
            binding.tokenInfoLayout.visible()
        } else {
            binding.allQrTransactionRecycler.visible()
            binding.tokenInfoLayout.gone()
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