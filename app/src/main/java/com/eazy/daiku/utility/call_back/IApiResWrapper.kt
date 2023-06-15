package com.eazy.daiku.utility.call_back

import com.google.gson.JsonObject

interface IApiResWrapper<T> {

    fun onLoading(hasLoading: Boolean);

    fun onData(respondData: T);

    fun onError(message: String, code: Int, errorHashMap: JsonObject)

}