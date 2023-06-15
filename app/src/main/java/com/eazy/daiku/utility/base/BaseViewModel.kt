package com.eazy.daiku.utility.base

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eazy.daiku.R
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.repository.Repository
import com.eazy.daiku.utility.call_back.IApiResWrapper
import com.eazy.daiku.utility.other.CatchServerErrorSingleTonJava
import com.eazy.daiku.utility.other.AppLOGG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

open class BaseViewModel
@Inject constructor(
    private val context: Context,
    open val repository: Repository
) : ViewModel() {

    val _globalErrorMutableLiveData =
        MutableLiveData<ApiResWraper<JsonElement>>()

    val globalErrorMutableLiveData: LiveData<ApiResWraper<JsonElement>> get() = _globalErrorMutableLiveData

    private fun errorHandle(respondError: JsonObject?): String {
        val keys = ArrayList<String>()
        val message = context.getString( R.string.something_went_wrong)
        respondError?.keySet()?.forEach {
            keys.add(it.toString())
        }
        respondError?.entrySet()?.forEach { (k, v) ->
            if (keys.contains(k)) {
                return v.asString
            }
        }
        return message
    }

    fun <T> submit(
        flowRequest: Flow<T>,
        iApiResWrapper: IApiResWrapper<T>
    ) {
        iApiResWrapper.onLoading(true)
        viewModelScope.launch {
            flowRequest.catch { error ->
                AppLOGG.d("ERROROROOROROR", Gson().toJson(error))
                iApiResWrapper.onLoading(false)
                CatchServerErrorSingleTonJava.catchError(
                    context,
                    error
                ) { message, code, errorKey ->
                    iApiResWrapper.onError(message, code, JsonObject())
                }

            }.collect { respondT ->
                iApiResWrapper.onLoading(false)
                iApiResWrapper.onData(respondT)
                if (respondT is ApiResWraper<*>) {
                    if (!respondT.success) {
                        iApiResWrapper.onError(
                            errorHandle(respondT.errors),
                            -1,
                            JsonObject()
                        )
                    }
                }
            }
        }
    }
}