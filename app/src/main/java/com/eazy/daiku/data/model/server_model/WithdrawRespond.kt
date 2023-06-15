package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class WithdrawRespond(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("uuid") var uuid: String? = null,
    @SerializedName("user_id") var userId: Int? = null,
    @SerializedName("amount") var amount: Int? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("kess_transaction_id") var kessTransactionId: String? = null,
    @SerializedName("transaction_id") var transactionId: Int? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("bic") var bic: String? = null,
    @SerializedName("remark") var remark: String? = null,
)