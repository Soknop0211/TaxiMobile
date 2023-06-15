package com.eazy.daiku.data.model.base

import com.google.gson.JsonObject

data class ApiResWraper<T>(
    val code: Int? = null,
    val message: String = "",
    val success: Boolean = false,
    val errors: JsonObject? = null,
    val data: T? = null

)