package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName


data class CustomerMeta(

    @SerializedName("kess_fees") var kessFees: ArrayList<CustomerKessFees> = arrayListOf(),
    @SerializedName("destination") var destination: CustomerDestination? = CustomerDestination(),
    @SerializedName("payment_type_fees") var paymentTypeFees: ArrayList<CustomerPaymentTypeFees> = arrayListOf(),
    @SerializedName("qr_code_url") var qrcodeurl: String? = null,
)