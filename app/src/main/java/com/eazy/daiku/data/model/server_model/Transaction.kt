package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

open class Transaction(
    @SerializedName("booking_code")
    var bookingCode: String? = null,
    @SerializedName("kess_transaction_id")
    var kessTransactionId: String? = null,
    @SerializedName("pay_by")
    var payBy: String? = null,
    @SerializedName("total")
    var total: Double? = null,
    @SerializedName("wallet_transaction_status")
    var walletTransactionStatus: String? = null,
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("user_withdraw")
    var userWithdraw: WithdrawRespond? = null,
    @SerializedName("type")
    var type: String? = null
)
