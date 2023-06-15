package com.eazy.daiku.ui.customer.model

import com.eazy.daiku.ui.customer.model.Storelink
import com.google.gson.annotations.SerializedName


data class WebPayPayment (

  @SerializedName("id"        ) var id        : Int?       = null,
  @SerializedName("title"     ) var title     : String?    = null,
  @SerializedName("img_url"   ) var imgUrl    : String?    = null,
  @SerializedName("bic"       ) var bic       : String?    = null,
  @SerializedName("storelink" ) var storelink : Storelink? = Storelink()

)