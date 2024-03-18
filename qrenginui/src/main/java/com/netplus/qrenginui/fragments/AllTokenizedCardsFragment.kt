package com.netplus.qrenginui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.netplus.qrengine.backendRemote.model.qr.EncryptedQrModel
import com.netplus.qrengine.utils.TallSecurityUtil
import com.netplus.qrengine.utils.TallyAppPreferences
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.activities.SingleQrTransactionsActivity
import com.netplus.qrenginui.adapters.TokenizedCardsAdapter
import com.netplus.qrenginui.databinding.AllTokenizedCardsBinding
import com.netplus.qrenginui.utils.ProgressDialogUtil
import com.netplus.qrenginui.utils.launchActivityWithExtra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllTokenizedCardsFragment : Fragment(), TokenizedCardsAdapter.Interaction {

    private lateinit var binding: AllTokenizedCardsBinding
    private lateinit var tokenizedCardsAdapter: TokenizedCardsAdapter
    private val progressDialogUtil by lazy { ProgressDialogUtil(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_all_tokenized_cards,
            container,
            false
        )

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initRecycler()
    }

    private fun initRecycler() {
        val partnerId = TallyAppPreferences.getInstance(requireContext())
            .getStringValue(TallyAppPreferences.PARTNER_ID)
        progressDialogUtil.showProgressDialog("Loading...")
        Handler(Looper.getMainLooper()).postDelayed({
            viewLifecycleOwner.lifecycleScope.launch {
                val tokenizedCardsData = withContext(Dispatchers.IO) {
                    TallSecurityUtil.retrieveData(requireContext())
                }

                if (tokenizedCardsData?.isEmpty() == true) {
                    switchViewVisibility(true)
                } else {
                    switchViewVisibility(false)
                    binding.tokenizedCardsRecycle.layoutManager =
                        LinearLayoutManager(requireContext())
                    tokenizedCardsAdapter = TokenizedCardsAdapter(
                        this@AllTokenizedCardsFragment,
                        tokenizedCardsData ?: emptyList()
                    )
                    binding.tokenizedCardsRecycle.adapter = tokenizedCardsAdapter

                    binding.tokenizedCardsRecycle.viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            // At this point, the RecyclerView is displayed on the UI
                            // Do something here
                            progressDialogUtil.dismissProgressDialog()

                            // Don't forget to remove the listener to prevent being called multiple times
                            binding.tokenizedCardsRecycle.viewTreeObserver.removeOnGlobalLayoutListener(
                                this
                            )
                        }
                    })
                }
            }
        }, 1000)
    }

    override fun onItemSelected(
        absoluteAdapterPosition: Int,
        encryptedQrModel: EncryptedQrModel
    ) {
        launchActivityWithExtra<SingleQrTransactionsActivity>(
            "qrcode_id",
            encryptedQrModel.qrcodeId.toString()
        )
    }

    private fun switchViewVisibility(isListEmpty: Boolean) {
        if (isListEmpty) {
            binding.tokenizedCardsRecycle.gone()
            binding.tokenInfoLayout.visible()
        } else {
            binding.tokenInfoLayout.gone()
            binding.tokenizedCardsRecycle.visible()
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