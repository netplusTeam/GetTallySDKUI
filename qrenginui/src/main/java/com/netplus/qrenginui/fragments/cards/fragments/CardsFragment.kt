package com.netplus.qrenginui.fragments.cards.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.netplus.qrengine.backendRemote.model.card.CheckOutResponse
import com.netplus.qrengine.backendRemote.model.card.PayPayload
import com.netplus.qrengine.backendRemote.model.card.PayResponse
import com.netplus.qrengine.backendRemote.model.keys.get.GetFinancialInstitutionKeyResponse
import com.netplus.qrengine.backendRemote.model.qr.EncryptedQrModel
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrPayload
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse
import com.netplus.qrengine.backendRemote.model.qr.store.StoreTokenizedCardsResponse
import com.netplus.qrengine.backendRemote.model.verve.VerveOtpPayload
import com.netplus.qrengine.utils.CURRENCY_TYPE
import com.netplus.qrengine.utils.GET_PARTNER_KEYS
import com.netplus.qrengine.utils.MERCHANT_ID
import com.netplus.qrengine.utils.TallSecurityUtil
import com.netplus.qrengine.utils.TallyAppPreferences
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
import com.netplus.qrengine.utils.showSnackbar
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.databinding.CardFragmentBinding
import com.netplus.qrenginui.utils.ProgressDialogUtil
import com.netplus.qrenginui.utils.WebAppInterface
import com.netplus.qrenginui.utils.cardSchemeLogos
import com.netplus.qrenginui.utils.createClientDataForNonVerveCard
import com.netplus.qrenginui.utils.createClientDataForVerveCard
import com.netplus.qrenginui.utils.generateOrderId
import com.netplus.qrenginui.utils.stringToBase64


class CardsFragment : Fragment() {

    private lateinit var binding: CardFragmentBinding
    private val tallyQrcodeGenerator = TallyQrcodeGenerator()
    private val progressDialogUtil by lazy { ProgressDialogUtil(requireContext()) }
    private lateinit var pinBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var otpBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var generateQrcodeResponse: GenerateQrcodeResponse? = null
    private lateinit var receiver: BroadcastReceiver

    private var userId: Int? = null
    private var email: String? = null
    private var fullName: String? = null
    private var issuingBank: String? = null
    private var mobilePhone: String? = null
    private var appCode: String? = null
    private var cardPin: String? = null
    private var cardScheme: String? = null
    private var verveOtp: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cards, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dismissBankWebView()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter("webAction"))

        initView()
        clickEvents()
        setUpBottomSheet()
    }

    private fun initView() {

        //set default state for bottom_sheet
        pinBottomSheetBehavior = BottomSheetBehavior.from(binding.pinBottomSheet)
        pinBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        otpBottomSheetBehavior = BottomSheetBehavior.from(binding.otpBottomSheet)
        otpBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        //automatically move cursor to next input field
        setEditTextListener(binding.pinOne, binding.pinTwo)
        setEditTextListener(binding.pinTwo, binding.pinThree)
        setEditTextListener(binding.pinThree, binding.pinFour)
        setEditTextListener(binding.pinFour, null)

        setEditTextListener(binding.otpOne, binding.otpTwo)
        setEditTextListener(binding.otpTwo, binding.otpThree)
        setEditTextListener(binding.otpThree, binding.otpFour)
        setEditTextListener(binding.otpFour, binding.otpFive)
        setEditTextListener( binding.otpFive,  binding.otpSix)
        setEditTextListener( binding.otpSix, null)
    }

    private fun clickEvents() {

        binding.bg.setOnClickListener { hideBottomSheet() }

        binding.generateQrButton.setOnClickListener {
            val inputtedCardNumber = binding.etCardNumber.text.toString().replace(" - ", "")
            val expiryMonth = binding.etMm.text.toString().toIntOrNull() ?: 0
            val expiryYear = binding.etYy.text.toString().toIntOrNull() ?: 0
            //val cardCvvString = cardCvv.text.toString()
            validateCardInformation(
                inputtedCardNumber,
                expiryMonth,
                expiryYear
            )
        }

        binding.etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val cardNumberString = s.toString().replace(" - ", "")
                val formattedCardNumber = formatCardNumber(cardNumberString)
                binding.etCardNumber.removeTextChangedListener(this)
                binding.etCardNumber.setText(formattedCardNumber)
                binding.etCardNumber.setSelection(formattedCardNumber.length)
                binding.etCardNumber.addTextChangedListener(this)

                //add card scheme logo based on card type returned
                when (getCardType(cardNumberString)) {
                    "Visa" -> {
                        binding.cardLogo.setImageResource(cardSchemeLogos[2])
                    }

                    "MasterCard" -> {
                        binding.cardLogo.setImageResource(cardSchemeLogos[0])
                    }

                    "American Express" -> {}

                    "Discover" -> {}

                    "Verve" -> {
                        binding.cardLogo.setImageResource(cardSchemeLogos[1])
                    }

                    "Unknown" -> {
                        binding.cardLogo.setImageResource(R.drawable.tally_logo)
                        binding.etCardNumber.error = "Invalid card number"
                    }
                }
            }
        })

        // concatenate inputted PAN
        binding.continueBtn.setOnClickListener {
            val inputtedCardPin =
                buildString {
                    append(binding.pinOne.text.toString())
                    append(binding.pinTwo.text.toString())
                    append(binding.pinThree.text.toString())
                    append(binding.pinFour.text.toString())
                }
            if (inputtedCardPin.length < 4) {
                requireContext().showSnackbar("PIN length is too short")
            } else {
                cardPin = inputtedCardPin
                performChargeOnCard(isPinInputted = true, inputtedCardPin)
            }
        }

        binding.verifyOtpBtn.setOnClickListener {
            val inputtedOtp = buildString {
                append(binding.otpOne.text.toString())
                append(binding.otpTwo.text.toString())
                append(binding.otpThree.text.toString())
                append(binding.otpFour.text.toString())
                append(binding.otpFive.text.toString())
                append(binding.otpSix.text.toString())
            }
            verveOtp = inputtedOtp
            if (inputtedOtp.length < 6) {
                requireContext().showSnackbar("OTP length is too short")
            } else {
                sendVerveOtp(inputtedOtp)
            }
        }
    }

    private fun validateCardInformation(
        inputtedCardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
    ) {
        val finUserId = TallyAppPreferences.getInstance(requireContext())
            .getStringValue(TallyAppPreferences.USER_ID).toString().toInt()
        val finUserEmail = TallyAppPreferences.getInstance(requireContext())
            .getStringValue(TallyAppPreferences.EMAIL)
        val finUserFullName = TallyAppPreferences.getInstance(requireContext())
            .getStringValue(TallyAppPreferences.FULL_NAME)
        val bankName = TallyAppPreferences.getInstance(requireContext())
            .getStringValue(TallyAppPreferences.BANK_NAME)
        val phoneNumber = TallyAppPreferences.getInstance(requireContext())
            .getStringValue(TallyAppPreferences.PHONE_NUMBER)

        val cardType = getCardType(inputtedCardNumber) // returns card scheme
        if (isValidExpiryDate(expiryMonth, expiryYear)) { //validates card expiry date
            if (isValidCardNumber(inputtedCardNumber, cardType)) { //validate if PAN is valid
                when (cardType) {
                    listOfCardSchemes[0] -> {
                        //Visa
                        userId = finUserId
                        email = finUserEmail
                        fullName = finUserFullName
                        issuingBank = bankName
                        mobilePhone = phoneNumber
                        appCode = "Tally"
                        cardScheme = cardType
                        //openBottomSheet()
                        performChargeOnCard(
                            isPinInputted = false,
                            inputtedCardPin = null
                        )
                    }

                    listOfCardSchemes[1] -> {
                        //MasterCard
                        userId = finUserId
                        email = finUserEmail
                        fullName = finUserFullName
                        issuingBank = bankName
                        mobilePhone = phoneNumber
                        appCode = "Tally"
                        cardScheme = cardType
                        performChargeOnCard(
                            isPinInputted = false,
                            inputtedCardPin = null
                        )
                        //generateQrcode(isPinInputted = false)
                    }

                    listOfCardSchemes[2] -> {
                        //American Express
                    }

                    listOfCardSchemes[3] -> {
                        //Discover
                    }

                    listOfCardSchemes[4] -> {
                        //Verve
                        userId = finUserId
                        email = finUserEmail
                        fullName = finUserFullName
                        issuingBank = bankName
                        mobilePhone = phoneNumber
                        appCode = "Tally"
                        cardScheme = cardType
                        openBottomSheet()
                    }
                }
            } else {
                binding.etCardNumber.error = "Invalid card number"
            }
        } else {
            binding.etYy.error = "Invalid date"
            binding.etMm.error = "Invalid date"
        }
    }

    private fun performChargeOnCard(isPinInputted: Boolean, inputtedCardPin: String?) {
        progressDialogUtil.showProgressDialog("Tokenizing card...")
        tallyQrcodeGenerator.cardCheckOut(
            merchantId = MERCHANT_ID,
            name = fullName.toString(),
            email = email.toString(),
            amount = 1.0,
            currency = CURRENCY_TYPE,
            orderId = generateOrderId(),
            object : TallyResponseCallback<CheckOutResponse> {
                override fun failed(message: String?) {
                    progressDialogUtil.showProgressDialog(message.toString())
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialogUtil.dismissProgressDialog()
                    }, 2000)
                }

                override fun success(data: CheckOutResponse?) {
                    makePayment(data, isPinInputted, inputtedCardPin.toString())
                }
            })
    }

    private fun makePayment(
        data: CheckOutResponse?,
        isPinInputted: Boolean,
        inputtedCardPin: String
    ) {
        val cardExpiry = buildString {
            append(binding.etMm.text.toString())
            append(binding.etYy.text.toString())
        }
        val generateQrPayload = GenerateQrPayload(
            user_id = userId,
            card_cvv = binding.etCvv.text.toString(),
            card_expiry = cardExpiry,
            card_number = binding.etCardNumber.text.toString().replace(" - ", ""),
            card_scheme = cardScheme.toString(),
            email = email,
            fullname = fullName,
            issuing_bank = issuingBank.toString(),
            mobile_phone = mobilePhone,
            app_code = appCode,
            card_pin = if (isPinInputted) inputtedCardPin else null
        )
        val clientDataString = if (cardScheme.toString().contains("verve", true)) {
            createClientDataForVerveCard(
                generateQrPayload,
                data?.transId.toString()
            )
        } else {
            createClientDataForNonVerveCard(
                data?.transId.toString(),
                generateQrPayload.card_number,
                generateQrPayload.card_expiry,
                generateQrPayload.card_cvv
            )
        }
        val clientData = stringToBase64(clientDataString).replace("\n", "")
        val payPayload = PayPayload(clientData, "PAY")

        if (isPinInputted) {
            tallyQrcodeGenerator.makeVerveCardPayment(
                payPayload,
                object : TallyResponseCallback<JsonObject> {
                    override fun failed(message: String?) {
                        progressDialogUtil.dismissProgressDialog()
                        requireContext().showSnackbar(message.toString())
                    }

                    override fun success(data: JsonObject?) {
                        val code = Gson().fromJson(data, PayResponse::class.java)
                        when (code?.code) {
                            "1" -> {
                                progressDialogUtil.dismissProgressDialog()
                                hideOtpBottomSheet()
                                clearForm()
                                requireContext().showSnackbar(message = "There was an error processing this transaction")
                            }
                            "80" -> {}
                            "90" -> {}
                            else -> {
                                progressDialogUtil.dismissProgressDialog()
                                hideBottomSheet()
                                openOtpBottomSheet()
                            }
                        }
                    }
                })
        } else {
            tallyQrcodeGenerator.makePayment(
                payPayload,
                object : TallyResponseCallback<PayResponse> {
                    override fun failed(message: String?) {
                        progressDialogUtil.showProgressDialog(message.toString())
                        Handler(Looper.getMainLooper()).postDelayed({
                            progressDialogUtil.dismissProgressDialog()
                        }, 3000)
                    }

                    override fun success(data: PayResponse?) {
                        progressDialogUtil.dismissProgressDialog()
                        switchViewVisibility(isWebViewDisplayed = true)
                        setUpBankWebView(
                            data?.TermUrl,
                            data?.MD,
                            data?.PaReq,
                            data?.ACSUrl,
                            data?.transId,
                            data?.redirectHtml
                        )
                    }
                })
        }
    }

    private fun sendVerveOtp(inputtedOtp: String) {
        val verveOtpPayload = VerveOtpPayload(
            OTPData = inputtedOtp,
            type = "OTP"
        )
        tallyQrcodeGenerator.sendOtpForVerveCard(
            verveOtpPayload = verveOtpPayload,
            object : TallyResponseCallback<JsonObject> {
                override fun failed(message: String?) {
                    requireContext().showSnackbar(message.toString())
                }

                override fun success(data: JsonObject?) {
                    //switchViewVisibility(isWebViewDisplayed = false)
                    progressDialogUtil.showProgressDialog("Processing...")
                    generateQrcode(isPinInputted = true)
                }
            })
    }

    private fun generateQrcode(isPinInputted: Boolean) {
        //progressDialogUtil.dismissProgressDialog()
        val cardExpiry = buildString {
            append(binding.etMm.text.toString())
            append(binding.etYy.text.toString())
        }
        tallyQrcodeGenerator.generateQrcode(
            userId = userId ?: 0,
            cardCvv = binding.etCvv.text.toString(),
            cardExpiry = cardExpiry,
            cardNumber = binding.etCardNumber.text.toString().replace(" - ", ""),
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
                }

                override fun failed(message: String?) {
                    progressDialogUtil.dismissProgressDialog()
                    requireContext().showSnackbar(message = message.toString())
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
        //val partnerId = TallyAppPreferences.getInstance(requireContext()).getStringValue(TallyAppPreferences.PARTNER_ID)
        if (data != null) {
            val encryptedQrModel = EncryptedQrModel(
                qrcodeId = data.qr_code_id,
                image = encryptBase64(data.data.toString(), data.qr_code_id.toString()),
                success = data.success,
                cardScheme = data.card_scheme,
                issuingBank = data.issuing_bank,
                date = data.date
            )
            TallSecurityUtil.storeData(requireContext(), encryptedQrModel, "Tally")
            Handler(Looper.getMainLooper()).postDelayed({
                clearForm()
                progressDialogUtil.dismissProgressDialog()
                val intent = Intent("swipeAction").apply {
                    putExtra("generateQrcodeResponse", data)
                }
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }, 1000)

            //getFinancialInstitutionKey(encryptedQrModel, data)
        }
    }

    private fun getFinancialInstitutionKey(
        encryptedQrModel: EncryptedQrModel,
        qrResponse: GenerateQrcodeResponse
    ) {
        tallyQrcodeGenerator.getGenerateFinancialInstitutionKeys(
            GET_PARTNER_KEYS,
            encryptedQrModel.issuingBank.toString(),
            object : TallyResponseCallback<GetFinancialInstitutionKeyResponse> {
                override fun failed(message: String?) {
                    progressDialogUtil.dismissProgressDialog()
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }

                override fun success(data: GetFinancialInstitutionKeyResponse?) {

                    Toast.makeText(
                        requireContext(),
                        "Card tokenization complete",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    progressDialogUtil.dismissProgressDialog()
                    TallyAppPreferences.getInstance(requireContext()).setStringValue(
                        TallyAppPreferences.PARTNER_ID,
                        data?.data?.secret.toString()
                    )
                    TallSecurityUtil.storeData(
                        requireContext(),
                        encryptedQrModel,
                        data?.data?.secret.toString()
                    )
                    val intent = Intent("swipeAction").apply {
                        putExtra("generateQrcodeResponse", qrResponse)
                    }
                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                }
            }
        )
    }

    private fun setUpBankWebView(
        termUrl: String?,
        md: String?,
        paReq: String?,
        acsUrl: String?,
        transId: String?,
        redirectHtml: String?
    ) {
        binding.webview.apply {
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
            }
            addJavascriptInterface(
                WebAppInterface(
                    mContext = requireContext(),
                    termUrl = termUrl.toString(),
                    md = md.toString(),
                    cReq = paReq.toString(),
                    ascReq = acsUrl.toString(),
                    transactionId = transId.toString(),
                    redirectHtml = redirectHtml.toString()
                ),
                "Android"
            )
            loadUrl("file:///android_asset/3ds_pay.html")
        }
    }

    private fun dismissBankWebView() {
        receiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "webAction") {
                    switchViewVisibility(isWebViewDisplayed = false)
                    progressDialogUtil.showProgressDialog("Processing...")
                    val code = intent.getStringExtra("code")
                    if (code.toString().isNotEmpty() && code != "") {
                        when (code) {
                            "00" -> {
                                //success
                                generateQrcode(isPinInputted = false)
                            }

                            "90" -> {
                                //fail
                                clearForm()
                                requireContext().showSnackbar(message = "Unable to charge card, please try again", length = 3000)
                            }

                            "80" -> {
                                //fail
                                clearForm()
                                requireContext().showSnackbar(message = "Unable to charge card, please try again", length = 3000)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun clearForm() {
        binding.etCardNumber.text?.clear()
        binding.etYy.text?.clear()
        binding.etMm.text?.clear()
        binding.etCvv.text?.clear()
    }

    private fun setUpBottomSheet() {
        pinBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(sheet: View, offset: Float) {
                binding.bg.visibility = View.VISIBLE
            }

            override fun onStateChanged(p0: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> binding.bg.visible()
                    BottomSheetBehavior.STATE_EXPANDED -> binding.bg.visible()
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> binding.bg.visible()
                    BottomSheetBehavior.STATE_HIDDEN -> binding.bg.gone()
                    BottomSheetBehavior.STATE_DRAGGING -> binding.bg.visible()
                    BottomSheetBehavior.STATE_SETTLING -> binding.bg.visible()
                }
            }
        })

        otpBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED ->  binding.bg.visible()
                    BottomSheetBehavior.STATE_EXPANDED ->  binding.bg.visible()
                    BottomSheetBehavior.STATE_HALF_EXPANDED ->  binding.bg.visible()
                    BottomSheetBehavior.STATE_HIDDEN ->  binding.bg.gone()
                    BottomSheetBehavior.STATE_DRAGGING ->  binding.bg.visible()
                    BottomSheetBehavior.STATE_SETTLING ->  binding.bg.visible()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.bg.visible()
            }
        })
    }

    private fun openBottomSheet() {
        binding.bg.visible()
        binding.pinBottomSheet.visible()
        pinBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideBottomSheet() {
        binding.pinOne.text?.clear()
        binding.pinTwo.text?.clear()
        binding.pinThree.text?.clear()
        binding.pinFour.text?.clear()
        pinBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun openOtpBottomSheet() {
        binding.bg.visible()
        binding.otpBottomSheet.visible()
        otpBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideOtpBottomSheet() {
        binding.otpOne.text?.clear()
        binding.otpTwo.text?.clear()
        binding.otpThree.text?.clear()
        binding.otpFour.text?.clear()
        binding.otpFive.text?.clear()
        binding.otpSix.text?.clear()
        otpBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun switchViewVisibility(isWebViewDisplayed: Boolean) {
        if (isWebViewDisplayed) {
            binding.webview.visible()
            binding.cardInfoLayout.gone()
        } else {
            binding.webview.gone()
            binding.cardInfoLayout.visible()
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