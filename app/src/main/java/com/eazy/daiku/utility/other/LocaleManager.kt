package com.eazy.daiku.utility.other

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringDef
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.utility.Config
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

object LocaleManager {

    const val ENGLISH = "en"
    const val KHMER = "km"
    const val CHINA = "zh"
    const val LANGUAGE_KEY = "language_key"

    @Retention(RetentionPolicy.SOURCE)
    @StringDef(ENGLISH, KHMER,CHINA)
    annotation class LocaleDef {
        companion object {
            var SUPPORTED_LOCALES = arrayOf(ENGLISH, KHMER,CHINA)
        }
    }

    fun getLanguagePref(context: Context): String? {
        val prefs = context.getSharedPreferences(Config.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        val language = prefs.getString(LANGUAGE_KEY, ENGLISH)
        if (language != null) {
            UserDefaultSingleTon.newInstance?.localizeLanguage = language
        }
        return language
    }

    private fun setLanguagePref(mContext: Context, localeKey: String) {
        UserDefaultSingleTon.newInstance?.localizeLanguage = localeKey
        val mPreferences =
            mContext.getSharedPreferences(Config.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        mPreferences.edit().putString(LANGUAGE_KEY, localeKey).apply()
    }

    /**
     * set current pref locale
     */
    fun setLocale(mContext: Context): Context {
        return updateResources(mContext, getLanguagePref(mContext))
    }

    /**
     * Set new Locale with context
     */
    fun setNewLocale(mContext: Context, @LocaleDef language: String): Context {
        setLanguagePref(mContext, language)
        return updateResources(mContext, language)
    }

    /**
     * update resource
     */
    private fun updateResources(context: Context, language: String?): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}