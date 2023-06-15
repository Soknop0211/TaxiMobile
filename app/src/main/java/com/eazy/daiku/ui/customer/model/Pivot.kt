package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class Pivot (

  @SerializedName("device_id" ) var deviceId : Int? = null,
  @SerializedName("term_id"   ) var termId   : Int? = null

)