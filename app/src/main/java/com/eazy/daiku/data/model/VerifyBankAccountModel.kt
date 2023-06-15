package com.eazy.daiku.data.model

import com.google.gson.annotations.SerializedName


data class VerifyBankAccountModel(

    @SerializedName("owner_name") var ownerName: String? = null,
    @SerializedName("currency") var currency: String? = null,
    @SerializedName("account_number") var accountNumber: String? = null

)