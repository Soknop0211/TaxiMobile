package com.eazy.daiku.utility.base

data class PaginationData<T>(
    val current_page: Int,
    val data: T,
    val first_page_url: String,
    val from: Int,
    val last_page: Int,
    val last_page_url: String,
    val path: String,
    val per_page: String,
    val to: Int,
    val total: Int
)