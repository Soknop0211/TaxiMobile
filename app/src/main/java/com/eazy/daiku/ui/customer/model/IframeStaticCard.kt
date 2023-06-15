package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class IframeStaticCard (

  @SerializedName("input_card"         ) var inputCard        : Boolean? = null,
  @SerializedName("card_number"        ) var cardNumber       : String?  = null,
  @SerializedName("card_security_code" ) var cardSecurityCode : String?  = null,
  @SerializedName("card_expiry_month"  ) var cardExpiryMonth  : String?  = null,
  @SerializedName("card_expiry_year"   ) var cardExpiryYear   : String?  = null,
  @SerializedName("card_bank_type"     ) var cardBankType     : String?  = null,
  @SerializedName("card_alias"         ) var cardAlias        : String?  = null

)