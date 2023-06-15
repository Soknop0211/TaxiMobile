package com.eazy.daiku.data.model

import com.google.gson.annotations.SerializedName


data class ListAllBankAccountModel (

  @SerializedName("id"             ) var id            : Int?    = null,
  @SerializedName("account_number" ) var accountNumber : String? = null,
  @SerializedName("bic"            ) var bic           : String? = null,
  @SerializedName("meta"           ) var meta          : Meta?   = Meta()

)