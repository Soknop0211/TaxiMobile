package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class CustomerFees (

  @SerializedName("POS_ACLBKHPP" ) var POSACLBKHPP : POSACLBKHPP? = POSACLBKHPP(),
  @SerializedName("POS_ABAAKHPP" ) var POSABAAKHPP : POSABAAKHPP? = POSABAAKHPP(),
  @SerializedName("POS_SBPLKHPP" ) var POSSBPLKHPP : POSSBPLKHPP? = POSSBPLKHPP(),
  @SerializedName("WECHAT"       ) var WECHAT      : WECHAT?      = WECHAT(),
  @SerializedName("ALIPAY"       ) var ALIPAY      : ALIPAY?      = ALIPAY()

)