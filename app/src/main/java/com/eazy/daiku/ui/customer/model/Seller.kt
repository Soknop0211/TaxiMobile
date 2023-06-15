package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class Seller (

  @SerializedName("code"         ) var code        : String? = null,
  @SerializedName("display_name" ) var displayName : String? = null

)