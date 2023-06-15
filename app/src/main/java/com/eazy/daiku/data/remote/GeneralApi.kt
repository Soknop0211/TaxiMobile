package com.eazy.daiku.data.remote

import com.github.kotlintelegrambot.entities.Message
import com.google.gson.JsonElement
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface GeneralApi {
    @GET("/")
    fun getPublicIp(): Call<JsonElement>

}