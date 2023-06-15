package com.eazy.daiku.data.model

import com.google.gson.annotations.SerializedName


data class PaymentDetail (

  @SerializedName("id"                 ) var id               : Int?    = null,
  @SerializedName("payer_id"           ) var payerId          : Int?    = null,
  @SerializedName("method_id"          ) var methodId         : Int?    = null,
  @SerializedName("tokenize_id"        ) var tokenizeId       : Int?    = null,
  @SerializedName("method_desc"        ) var methodDesc       : String? = null,
  @SerializedName("holder_name"        ) var holderName       : String? = null,
  @SerializedName("created_at"         ) var createdAt        : String? = null,
  @SerializedName("payment_method_bic" ) var paymentMethodBic : String? = null

)