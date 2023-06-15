package com.eazy.daiku.ui.customer.model.sear_map

import com.google.gson.annotations.SerializedName


data class SearchMapModel (

  @SerializedName("results" ) var results : ArrayList<Results> = arrayListOf(),
  @SerializedName("status"  ) var status  : String?            = null,
  @SerializedName("success" ) var success : Boolean?           = null

)