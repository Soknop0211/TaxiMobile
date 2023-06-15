package com.eazy.daiku.utility.other

import android.util.Log
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.Constants

object AppLOGG {
    val ASSERT = 7
    val DEBUG = 3
    val ERROR = 6
    val INFO = 4
    val VERBOSE = 2
    val WARN = 5

    private val isLoggable: Boolean = BuildConfig.BUILD_TYPE == "debug"

    fun d(tag: String, msg: String) {
        if (!isLoggable) return
        Log.d(tag, String.format("%s", msg))
    }

    fun d(tag: String?, msg: String?, tr: Throwable?) {
        if (!isLoggable) return
        Log.d(tag, String.format("%s", msg), tr)
    }

    fun i(tag: String?, msg: String) {
        if (!isLoggable) return
        Log.i(tag, String.format("%s", msg))
    }

    fun i(tag: String?, msg: String?, tr: Throwable?) {
        if (!isLoggable) return
        Log.i(tag, String.format("%s", msg), tr)
    }

    fun w(tag: String?, msg: String) {
        if (!isLoggable) return
        Log.w(tag, String.format("%s", msg))
    }

    fun w(tag: String?, msg: String?, tr: Throwable?) {
        if (!isLoggable) return
        Log.w(tag, String.format("%s", msg), tr)
    }

    fun w(tag: String?, tr: Throwable?) {
        if (!isLoggable) return
        Log.w(tag, tr)
    }

    fun e(tag: String?, msg: String) {
        if (!isLoggable) return
        Log.e(tag, String.format("%s", msg))
    }

    fun e(tag: String?, msg: String?, tr: Throwable?) {
        if (!isLoggable) return
        Log.e(tag, String.format("%s", msg), tr)
    }

    fun wtf(tag: String?, msg: String?) {
        if (!isLoggable) return
        Log.wtf(tag, String.format("%s", msg))
    }

    fun wtf(tag: String?, tr: Throwable) {
        if (!isLoggable) return
        Log.wtf(tag, tr)
    }

    fun wtf(tag: String?, msg: String?, tr: Throwable?) {
        if (!isLoggable) return
        Log.wtf(tag, String.format("%s", msg), tr)
    }
}