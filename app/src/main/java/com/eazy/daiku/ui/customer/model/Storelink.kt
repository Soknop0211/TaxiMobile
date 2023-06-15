package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class Storelink (

  @SerializedName("ios"     ) var ios     : String? = null,
  @SerializedName("android" ) var android : String? = null

)