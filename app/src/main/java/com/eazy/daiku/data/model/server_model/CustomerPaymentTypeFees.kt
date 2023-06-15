package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName


data class CustomerPaymentTypeFees (

  @SerializedName("name"  ) var name  : String? = null,
  @SerializedName("type"  ) var type  : String? = null,
  @SerializedName("unit"  ) var unit  : String? = null,
  @SerializedName("price" ) var price : String? = null

)