package com.eazy.daiku.data.remote

import com.eazy.daiku.data.model.server_model.TelegramRespondBody
import com.github.kotlintelegrambot.entities.Message
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TelegramApi {
    @POST("sendMessage")
    @Headers(
        "Content-Type:application/x-www-form-urlencoded"
    )
    fun submitErrorTelegramDefault(@QueryMap map: HashMap<String, Any>): Call<TelegramRespondBody>
    @POST("sendPhoto")
    fun sendPhoto(@QueryMap map: HashMap<String, Any>, @Body requestBody: RequestBody): Call<Response<Message>>

}