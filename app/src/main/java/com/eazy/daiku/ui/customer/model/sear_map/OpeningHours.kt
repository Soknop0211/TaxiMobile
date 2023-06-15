package com.eazy.daiku.ui.customer.model.sear_map

import com.google.gson.annotations.SerializedName


data class OpeningHours (

  @SerializedName("open_now" ) var openNow : Boolean? = null

)