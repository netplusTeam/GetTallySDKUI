package com.netplus.qrenginui.fragments.cards.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.netplus.qrengine.backendRemote.model.qr.EncryptedQrModel
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse
import com.netplus.qrengine.backendRemote.model.qr.store.StoreTokenizedCardsResponse
import com.netplus.qrengine.utils.TallSecurityUtil
import com.netplus.qrengine.utils.TallyQrcodeGenerator
import com.netplus.qrengine.utils.TallyResponseCallback
import com.netplus.qrengine.utils.encryptBase64
import com.netplus.qrengine.utils.formatCardNumber
import com.netplus.qrengine.utils.getCardType
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.isValidCardNumber
import com.netplus.qrengine.utils.isValidExpiryDate
import com.netplus.qrengine.utils.listOfCardSchemes
import com.netplus.qrengine.utils.setEditTextListener
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.utils.ProgressDialogUtil


class CardsFragment : Fragment() {

    private val tallyQrcodeGenerator = TallyQrcodeGenerator()
    private val progressDialogUtil by lazy { ProgressDialogUtil(requireContext()) }
    private lateinit var pinBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var cardNumber: AppCompatEditText
    private lateinit var cardExpiryMonth: AppCompatEditText
    private lateinit var cardExpiryYear: AppCompatEditText
    private lateinit var cardCvv: AppCompatEditText
    private lateinit var generateQrBtn: AppCompatButton
    private lateinit var continueButton: AppCompatButton
    private lateinit var pinBottomSheet: ConstraintLayout
    private lateinit var background: View
    private lateinit var firstPin: AppCompatEditText
    private lateinit var secondPin: AppCompatEditText
    private lateinit var thirdPin: AppCompatEditText
    private lateinit var forthPin: AppCompatEditText
    private var generateQrcodeResponse: GenerateQrcodeResponse? = null

    private var userId: Int? = null
    private var email: String? = null
    private var fullName: String? = null
    private var issuingBank: String? = null
    private var mobilePhone: String? = null
    private var appCode: String? = null
    private var cardPin: String? = null
    private var cardScheme: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_cards, container, false)

        cardNumber = rootView.findViewById(R.id.etCardNumber)
        cardExpiryMonth = rootView.findViewById(R.id.et_mm)
        cardExpiryYear = rootView.findViewById(R.id.et_yy)
        cardCvv = rootView.findViewById(R.id.et_cvv)
        generateQrBtn = rootView.findViewById(R.id.generate_qr_button)
        continueButton = rootView.findViewById(R.id.continue_btn)
        pinBottomSheet = rootView.findViewById(R.id.pin_bottom_sheet)
        background = rootView.findViewById(R.id.bg)
        firstPin = rootView.findViewById(R.id.pin_one)
        secondPin = rootView.findViewById(R.id.pin_two)
        thirdPin = rootView.findViewById(R.id.pin_three)
        forthPin = rootView.findViewById(R.id.pin_four)

        initView()
        clickEvents()
        setUpBottomSheet()

        return rootView
    }

    private fun initView() {
        //set default state for bottom_sheet
        pinBottomSheetBehavior = BottomSheetBehavior.from(pinBottomSheet)
        pinBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        //automatically move cursor to next input field
        setEditTextListener(firstPin, secondPin)
        setEditTextListener(secondPin, thirdPin)
        setEditTextListener(thirdPin, forthPin)
        setEditTextListener(forthPin, null)
    }

    private fun clickEvents() {

        background.setOnClickListener { hideBottomSheet() }

        generateQrBtn.setOnClickListener {
            val inputtedCardNumber = cardNumber.text.toString().replace(" - ", "")
            val expiryMonth = cardExpiryMonth.text.toString().toIntOrNull() ?: 0
            val expiryYear = cardExpiryYear.text.toString().toIntOrNull() ?: 0
            val cardCvvString = cardCvv.text.toString()
            validateCardInformation(
                inputtedCardNumber,
                expiryMonth,
                expiryYear
            )
        }

        cardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val cardNumberString = s.toString().replace(" - ", "")
                val formattedCardNumber = formatCardNumber(cardNumberString)
                cardNumber.removeTextChangedListener(this)
                cardNumber.setText(formattedCardNumber)
                cardNumber.setSelection(formattedCardNumber.length)
                cardNumber.addTextChangedListener(this)
            }
        })

        continueButton.setOnClickListener {
            val inputtedCardPin =
                buildString {
                    append(firstPin.text.toString().isNotEmpty())
                    append(secondPin.text.toString().isNotEmpty())
                    append(thirdPin.text.toString().isNotEmpty())
                    append(forthPin.text.toString().isNotEmpty())
                }
            cardPin = inputtedCardPin
            generateQrcode(isPinInputted = true)
        }
    }

    private fun validateCardInformation(
        inputtedCardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
    ) {
        val cardType = getCardType(inputtedCardNumber)
        if (isValidExpiryDate(expiryMonth, expiryYear)) {
            if (isValidCardNumber(inputtedCardNumber, cardType)) {
                when (cardType) {
                    listOfCardSchemes[0] -> {
                        //Visa
                        userId = 12
                        email = "nicholasanyanwu125@gmail.com"
                        fullName = "Nicholas"
                        issuingBank = "Wema"
                        mobilePhone = "09090909090"
                        appCode = "Tall"
                        cardScheme = cardType
                        openBottomSheet()
                    }

                    listOfCardSchemes[1] -> {
                        //MasterCard
                        userId = 12
                        email = "nicholasanyanwu125@gmail.com"
                        fullName = "Nicholas"
                        issuingBank = "Access Bank"
                        mobilePhone = "09090909090"
                        appCode = "Tall"
                        cardScheme = cardType
                        generateQrcode(isPinInputted = false)
                    }

                    listOfCardSchemes[2] -> {
                        //American Express
                    }

                    listOfCardSchemes[3] -> {
                        //Discover
                    }

                    listOfCardSchemes[4] -> {
                        //Verve
                        userId = 12
                        email = "nicholasanyanwu125@gmail.com"
                        fullName = "Nicholas"
                        issuingBank = "GTCO"
                        mobilePhone = "09090909090"
                        appCode = "Tall"
                        cardScheme = cardType
                        openBottomSheet()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Invalid card information", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            Toast.makeText(requireContext(), "Invalid card expiry date", Toast.LENGTH_LONG).show()
        }
    }

    private fun generateQrcode(isPinInputted: Boolean) {
        progressDialogUtil.showProgressDialog("Tokenizing card...")
        val cardExpiry = buildString {
            append(cardExpiryMonth.text.toString())
            append(cardExpiryYear.text.toString())
        }
        tallyQrcodeGenerator.generateQrcode(
            userId = userId ?: 0,
            cardCvv = cardCvv.text.toString(),
            cardExpiry = cardExpiry,
            cardNumber = cardNumber.text.toString().replace(" - ", ""),
            cardScheme = cardScheme ?: "",
            email = email ?: "",
            fullName = fullName ?: "",
            issuingBank = issuingBank ?: "",
            mobilePhone = mobilePhone ?: "",
            appCode = appCode ?: "",
            cardPin = (if (isPinInputted) cardPin else null) ?: "",
            object : TallyResponseCallback<GenerateQrcodeResponse> {
                override fun success(data: GenerateQrcodeResponse?) {
                    generateQrcodeResponse = data
                    //storeTokenizedCard(data)
                    encryptQrToken(data)
                    hideBottomSheet()
                    clearForm()
                }

                override fun failed(message: String?) {
                    progressDialogUtil.showProgressDialog(message.toString())
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialogUtil.dismissProgressDialog()
                    }, 2000)
                }
            })
    }

    private fun storeTokenizedCard(data: GenerateQrcodeResponse?) {
        tallyQrcodeGenerator.storeTokenizedCards(
            cardScheme = data?.card_scheme ?: "",
            email = email ?: "",
            issuingBank = data?.issuing_bank ?: "",
            qrCodeId = data?.qr_code_id ?: "",
            qrToken = data?.data ?: "",
            object : TallyResponseCallback<StoreTokenizedCardsResponse> {
                override fun success(data: StoreTokenizedCardsResponse?) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialogUtil.dismissProgressDialog()
                    }, 2000)
                }

                override fun failed(message: String?) {
                    progressDialogUtil.showProgressDialog(message.toString())
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialogUtil.dismissProgressDialog()
                    }, 2000)
                }
            })
    }

    private fun encryptQrToken(data: GenerateQrcodeResponse?) {
        if (data != null) {
            Toast.makeText(requireContext(), "Card tokenization complete", Toast.LENGTH_SHORT)
                .show()
            val encryptedQrModel = EncryptedQrModel(
                qrcodeId = data.qr_code_id,
                image = encryptBase64(data.data.toString(), data.qr_code_id.toString()),
                success = data.success,
                cardScheme = data.card_scheme,
                issuingBank = data.issuing_bank,
                date = data.date
            )
            progressDialogUtil.dismissProgressDialog()
            TallSecurityUtil.storeData(requireContext(), encryptedQrModel)
            val intent = Intent("swipeAction").apply {
                putExtra("generateQrcodeResponse", data)
            }
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
    }

    private fun clearForm() {
        cardNumber.text?.clear()
        cardExpiryYear.text?.clear()
        cardExpiryMonth.text?.clear()
        cardCvv.text?.clear()
    }

    private fun setUpBottomSheet() {
        pinBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(sheet: View, offset: Float) {
                background.visibility = View.VISIBLE
            }

            override fun onStateChanged(p0: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> background.visible()
                    BottomSheetBehavior.STATE_EXPANDED -> background.visible()
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> background.visible()
                    BottomSheetBehavior.STATE_HIDDEN -> background.gone()
                    BottomSheetBehavior.STATE_DRAGGING -> background.visible()
                    BottomSheetBehavior.STATE_SETTLING -> background.visible()
                }
            }
        })
    }

    private fun openBottomSheet() {
        background.visible()
        pinBottomSheet.visible()
        pinBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideBottomSheet() {
        firstPin.text?.clear()
        secondPin.text?.clear()
        thirdPin.text?.clear()
        forthPin.text?.clear()
        pinBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
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