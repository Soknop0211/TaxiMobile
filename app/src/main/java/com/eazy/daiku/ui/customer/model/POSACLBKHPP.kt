package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class POSACLBKHPP (

  @SerializedName("fee"      ) var fee      : Double? = null,
  @SerializedName("currency" ) var currency : String? = null

)