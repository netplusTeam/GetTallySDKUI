package com.netplus.qrenginui.fragments.cards.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse
import com.netplus.qrengine.utils.decodeBase64ToBitmap
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.saveImageToGallery
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.utils.DataTransferInterface

class RecentTokenizedCardFragment : Fragment(), DataTransferInterface {

    private lateinit var bankNameAndScheme: TextView
    private lateinit var dateGenerated: TextView
    private lateinit var qrImage: ImageView
    private lateinit var saveQr: AppCompatButton
    private lateinit var qrInfoLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_recent_tokenized_cards, container, false)

        bankNameAndScheme = rootView.findViewById(R.id.card_and_bank_scheme)
        dateGenerated = rootView.findViewById(R.id.date_created)
        qrImage = rootView.findViewById(R.id.tokenized_card_image)
        saveQr = rootView.findViewById(R.id.save_qr_on_device_btn)
        qrInfoLayout = rootView.findViewById(R.id.token_info_layout)


        clickEvents()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        //val tokenizedCardsData = TallSecurityUtil.retrieveData(requireContext())
    }

    private fun clickEvents() {
        saveQr.setOnClickListener {
            saveImageToGallery(requireContext(), qrImage)
            Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchViewVisibility(isAnyCardRecentlyTokenized: Boolean) {
        if (isAnyCardRecentlyTokenized) {
            qrInfoLayout.gone()
            qrImage.visible()
            bankNameAndScheme.visible()
            dateGenerated.visible()
            saveQr.visible()
        } else {
            qrImage.gone()
            bankNameAndScheme.gone()
            dateGenerated.gone()
            saveQr.gone()
            qrInfoLayout.visible()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        switchViewVisibility(false)
    }

    override fun onPause() {
        super.onPause()
        switchViewVisibility(false)
    }

    @SuppressLint("SetTextI18n")
    override fun transferData(generateQrcodeResponse: GenerateQrcodeResponse?) {
        if (generateQrcodeResponse != null) {
            val image = decodeBase64ToBitmap(
                generateQrcodeResponse.data.toString().substringAfter("data:image/png;base64,")
            )
            bankNameAndScheme.text =
                "${generateQrcodeResponse.issuing_bank} ${generateQrcodeResponse.card_scheme}"
            dateGenerated.text = generateQrcodeResponse.date
            qrImage.setImageBitmap(image)
            switchViewVisibility(true)
        } else {
            // Handle decoding error
            switchViewVisibility(false)
        }
    }
}