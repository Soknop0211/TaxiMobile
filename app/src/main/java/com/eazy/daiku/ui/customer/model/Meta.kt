package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class Meta (

    @SerializedName("setting"                   ) var setting              : Setting?          = Setting(),
    @SerializedName("out_trade_no_sent_to_KESS" ) var outTradeNoSentToKESS : ArrayList<String> = arrayListOf(),
    @SerializedName("customer_fees"             ) var customerFees         : CustomerFees?     = CustomerFees(),
    @SerializedName("iframe_static_card"        ) var iframeStaticCard     : IframeStaticCard? = IframeStaticCard()

)