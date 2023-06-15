package com.example.example

import com.google.gson.annotations.SerializedName


data class ServiceFees (

  @SerializedName("rate"         ) var rate        : String? = null,
  @SerializedName("label"        ) var label       : String? = null,
  @SerializedName("price"        ) var price       : Double? = null,
  @SerializedName("display_rate" ) var displayRate : String? = null

)