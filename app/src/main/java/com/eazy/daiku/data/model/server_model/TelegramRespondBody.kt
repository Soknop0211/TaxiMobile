package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TelegramRespondBody {
    @SerializedName("ok")
    @Expose
    var ok: Boolean? = null
    /*@SerializedName("result")
    @Expose
    var result: Result? = null*/
}
class Result {
    @SerializedName("message_id")
    @Expose
    var messageId: Int? = null

    @SerializedName("from")
    @Expose
    var from: From? = null

    @SerializedName("chat")
    @Expose
    var chat: Chat? = null

    @SerializedName("date")
    @Expose
    var date: Int? = null

    @SerializedName("text")
    @Expose
    var text: String? = null
}
class From {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("is_bot")
    @Expose
    var isBot: Boolean? = null

    @SerializedName("first_name")
    @Expose
    var firstName: String? = null

    @SerializedName("username")
    @Expose
    var username: String? = null
}
class Chat {
    @SerializedName("id")
    @Expose
    var id: Long? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null
}