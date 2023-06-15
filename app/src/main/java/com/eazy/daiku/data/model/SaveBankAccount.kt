package com.eazy.daiku.data.model

import java.util.*

data class SaveBankAccount(
    val userId: Int = -100,
    val uuid: String = "",
    val bic: String = "",
    val name: String = "",
    val number: String = "",
    val logo: Int = -1,
)