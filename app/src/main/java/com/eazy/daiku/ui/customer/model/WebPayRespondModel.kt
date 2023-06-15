package com.eazy.daiku.ui.customer.model

import com.eazy.daiku.ui.customer.model.Meta
import com.eazy.daiku.ui.customer.model.Seller
import com.eazy.daiku.ui.customer.model.WebPayPayment
import com.google.gson.annotations.SerializedName


data class WebPayRespondModel (

    @SerializedName("token"           ) var token         : String?                  = null,
    @SerializedName("out_trade_no"    ) var outTradeNo    : String?                  = null,
    @SerializedName("body"            ) var body          : String?                  = null,
    @SerializedName("total_amount"    ) var totalAmount   : Double?                  = null,
    @SerializedName("currency"        ) var currency      : String?                  = null,
    @SerializedName("meta"            ) var meta          : Meta?                    = Meta(),
    @SerializedName("status"          ) var status        : String?                  = null,
    @SerializedName("expired_at"      ) var expiredAt     : String?                  = null,
    @SerializedName("created_at"      ) var createdAt     : String?                  = null,
    @SerializedName("detail"          ) var detail        : ArrayList<String>        = arrayListOf(),
    @SerializedName("seller"          ) var seller        : Seller?                  = Seller(),
    @SerializedName("queue_number"    ) var queueNumber   : String?                  = null,
    @SerializedName("payment_link"    ) var paymentLink   : String?                  = null,
    @SerializedName("qr_code_url"     ) var qrCodeUrl     : String?                  = null,
    @SerializedName("from_location"   ) var fromLocation  : ArrayList<String>        = arrayListOf(),
    @SerializedName("to_location"     ) var toLocation    : ArrayList<String>        = arrayListOf(),
    @SerializedName("kilometre"       ) var kilometre     : Double?                  = null,
    @SerializedName("total_price"     ) var totalPrice    : Double?                  = null,
    @SerializedName("payment_type"    ) var paymentType   : ArrayList<String>        = arrayListOf(),
    @SerializedName("web_pay_payment" ) var webPayPayment : ArrayList<WebPayPayment> = arrayListOf()

)