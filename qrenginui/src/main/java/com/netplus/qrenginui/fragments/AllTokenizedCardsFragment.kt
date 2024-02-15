package com.netplus.qrenginui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netplus.qrengine.backendRemote.model.qr.EncryptedQrModel
import com.netplus.qrengine.utils.TallSecurityUtil
import com.netplus.qrengine.utils.TallyAppPreferences
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.activities.SingleQrTransactionsActivity
import com.netplus.qrenginui.adapters.TokenizedCardsAdapter
import com.netplus.qrenginui.utils.ProgressDialogUtil
import com.netplus.qrenginui.utils.launchActivityWithExtra
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllTokenizedCardsFragment : Fragment(), TokenizedCardsAdapter.Interaction {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tokenizedCardsAdapter: TokenizedCardsAdapter
    private lateinit var qrInfoLayout: LinearLayout
    private val progressDialogUtil by lazy { ProgressDialogUtil(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_all_tokenized_cards, container, false)

        initViews(rootView)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        initRecycler()
    }

    private fun initViews(rootView: View) {
        recyclerView = rootView.findViewById(R.id.tokenized_cards_recycle)
        qrInfoLayout = rootView.findViewById(R.id.token_info_layout)
    }

    private fun initRecycler() {
        Handler(Looper.getMainLooper()).postDelayed({
            progressDialogUtil.showProgressDialog("Loading...")
            viewLifecycleOwner.lifecycleScope.launch {
                val tokenizedCardsData = withContext(Dispatchers.IO) {
                    TallSecurityUtil.retrieveData(requireContext())
                }

                if (tokenizedCardsData?.isEmpty() == true) {
                    switchViewVisibility(true)
                } else {
                    switchViewVisibility(false)
                }

                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                tokenizedCardsAdapter = TokenizedCardsAdapter(
                    this@AllTokenizedCardsFragment,
                    tokenizedCardsData ?: emptyList()
                )
                recyclerView.adapter = tokenizedCardsAdapter

                progressDialogUtil.dismissProgressDialog()
            }
        }, 2000)
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
            recyclerView.gone()
            qrInfoLayout.visible()
        } else {
            qrInfoLayout.gone()
            recyclerView.visible()
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