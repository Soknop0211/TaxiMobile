package com.eazy.daiku.data.model

import com.eazy.daiku.data.model.server_model.Transaction
import com.eazy.daiku.data.model.server_model.WithdrawRespond
import java.util.HashMap

class MyTransaction(
    bookingCode: String?,
    kessTransactionId: String?,
    payBy: String?,
    total: Double?,
    walletTransactionStatus: String?,
    createdAt: String?,
    var totalBalance: Double?,
    var blockedBalance: Double?,
    var totalAmountByDate: HashMap<String, Double>? = null,
    var showHeaderDate: Boolean? = false,
    var mUserWithdraw: WithdrawRespond? = null,
    var mType: String? = null
) : Transaction(
    bookingCode,
    kessTransactionId,
    payBy,
    total,
    walletTransactionStatus,
    createdAt,
    mUserWithdraw,
    mType
)