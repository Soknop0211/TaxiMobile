package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class TransactionRespond(
    @SerializedName("blocked_balance") var blockedBalance: Double? = null,
    @SerializedName("total_balance") var totalBalance: Double? = null,
    @SerializedName("transactions") var transactions: ArrayList<Transaction> = arrayListOf()
)