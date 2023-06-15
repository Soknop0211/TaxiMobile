package com.eazy.daiku.data.model.base

class ResponseErrorBody<T>(
    val code: Int,
    val error: T,
    val message: String,
    val success: Boolean
)