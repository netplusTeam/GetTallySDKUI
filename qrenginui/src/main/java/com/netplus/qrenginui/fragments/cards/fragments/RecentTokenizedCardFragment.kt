package com.netplus.qrenginui.fragments.cards.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse
import com.netplus.qrengine.utils.decodeBase64ToBitmap
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.saveImageToGallery
import com.netplus.qrengine.utils.showSnackbar
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.databinding.RecentTokenizedCardFragmentBinding
import com.netplus.qrenginui.utils.DataTransferInterface

class RecentTokenizedCardFragment : Fragment(), DataTransferInterface {

    private lateinit var binding: RecentTokenizedCardFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recent_tokenized_cards, container, false)

        clickEvents()

        return binding.root
    }

    private fun clickEvents() {
        binding.saveQrOnDeviceBtn.setOnClickListener {
            saveImageToGallery(requireContext(), binding.tokenizedCardImage)
            requireContext().showSnackbar(message = "Saved successfully")
        }
    }

    private fun switchViewVisibility(isAnyCardRecentlyTokenized: Boolean) {
        if (isAnyCardRecentlyTokenized) {
            binding.tokenInfoLayout.gone()
            binding.tokenizedCardImage.visible()
            binding.cardAndBankScheme.visible()
            binding.dateCreated.visible()
            binding.saveQrOnDeviceBtn.visible()
        } else {
            binding.tokenizedCardImage.gone()
            binding.cardAndBankScheme.gone()
            binding.dateCreated.gone()
            binding.saveQrOnDeviceBtn.gone()
            binding.tokenInfoLayout.visible()
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
            binding.cardAndBankScheme.text =
                "${generateQrcodeResponse.issuing_bank} ${generateQrcodeResponse.card_scheme}"
            binding.dateCreated.text = generateQrcodeResponse.date
            binding.tokenizedCardImage.setImageBitmap(image)
            switchViewVisibility(true)
        } else {
            // Handle decoding error
            switchViewVisibility(false)
        }
    }
}