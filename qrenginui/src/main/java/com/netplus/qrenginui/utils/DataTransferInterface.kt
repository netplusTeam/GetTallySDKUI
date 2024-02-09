package com.netplus.qrenginui.utils

import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse

interface DataTransferInterface {

    fun transferData(generateQrcodeResponse: GenerateQrcodeResponse?)
}