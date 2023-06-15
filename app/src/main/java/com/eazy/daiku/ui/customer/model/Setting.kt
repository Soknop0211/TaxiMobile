package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class Setting (

  @SerializedName("payment_type"            ) var paymentType           : String?           = null,
  @SerializedName("zoom_screen"             ) var zoomScreen            : Int?              = null,
  @SerializedName("enabled_payment_methods" ) var enabledPaymentMethods : ArrayList<String> = arrayListOf(),
  @SerializedName("template"                ) var template              : String?           = null

)