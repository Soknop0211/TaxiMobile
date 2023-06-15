package com.eazy.daiku.data.model

import com.google.gson.annotations.SerializedName


data class WithdrawMoneyRespondModel (

    @SerializedName("token"           ) var token         : String?           = null,
    @SerializedName("out_trade_no"    ) var outTradeNo    : String?           = null,
    @SerializedName("body"            ) var body          : String?           = null,
    @SerializedName("total_amount"    ) var totalAmount   : Double?              = null,
    @SerializedName("currency"        ) var currency      : String?           = null,
    @SerializedName("meta"            ) var meta          : ArrayList<String> = arrayListOf(),
    @SerializedName("status"          ) var status        : String?           = null,
    @SerializedName("expired_at"      ) var expiredAt     : String?           = null,
    @SerializedName("created_at"      ) var createdAt     : String?           = null,
    @SerializedName("detail"          ) var detail        : ArrayList<String> = arrayListOf(),
    @SerializedName("seller"          ) var seller        : Seller?           = Seller(),
    @SerializedName("payment_detail"  ) var paymentDetail : PaymentDetail?    = PaymentDetail(),
    @SerializedName("receiver_name"   ) var receiverName  : String?           = null,
    @SerializedName("is_required_otp" ) var isRequiredOtp : Boolean?          = null,
    @SerializedName("verify_otp_url"  ) var verifyOtpUrl  : String?           = null

)