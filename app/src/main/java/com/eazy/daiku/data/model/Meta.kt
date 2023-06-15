package com.eazy.daiku.data.model

import com.google.gson.annotations.SerializedName


data class Meta (

  @SerializedName("currency"       ) var currency      : String? = null,
  @SerializedName("owner_name"     ) var ownerName     : String? = null,
  @SerializedName("account_number" ) var accountNumber : String? = null

)