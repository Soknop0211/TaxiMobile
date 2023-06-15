package com.eazy.daiku.di.module

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.eazy.daiku.data.network.MyServiceInterceptor
import com.eazy.daiku.data.remote.EazyTaxiApi
import com.eazy.daiku.data.repository.Repository
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.EazyTaxiHelper
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 *created by Tona Song on 29-03-21.
 **/

@Module
object NetworkModule {


    @Provides
    @Singleton
    fun provideContext(app: Application): Context {
        return app.applicationContext
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val builder = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        return builder
            .setLenient()
            .create()
    }


    @Provides
    @Singleton
    fun provideCache(app: Application): Cache {
        return Cache(
            File(app.applicationContext.cacheDir, "caifutong_cache"),
            10 * 1024 * 1024
        )
    }

    @Provides
    @Singleton
    fun provideMyServiceInterceptor(context: Context): MyServiceInterceptor {
        return MyServiceInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideRequestHeader(
        cache: Cache,
        myServiceInterceptor: MyServiceInterceptor
    ): OkHttpClient {
        val timeOut:Long = 120
        val httpClient = OkHttpClient.Builder()
        httpClient.cache(cache)
        /*  httpClient.addNetworkInterceptor(cacheInterceptor)*/
        httpClient.addNetworkInterceptor(EazyTaxiHelper.KessLogDataGson())
        httpClient.addInterceptor(myServiceInterceptor)
        httpClient.connectTimeout(timeOut, TimeUnit.SECONDS)
        httpClient.writeTimeout(timeOut, TimeUnit.SECONDS)
        httpClient.readTimeout(timeOut, TimeUnit.SECONDS)
        return httpClient.build()
    }

    @Provides
    @Singleton
    fun provideRetrofitInstance(gson: Gson, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideCaiFuTongApi(retrofit: Retrofit): EazyTaxiApi {
        return retrofit.create(EazyTaxiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(context: Context, caiFuTongApi: EazyTaxiApi): Repository {
        return Repository(context,caiFuTongApi)
    }

    @Provides
    @Singleton
    fun provideGoogleApiAvailability() = GoogleApiAvailability.getInstance()

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        application: Application
    ) = LocationServices.getFusedLocationProviderClient(application)

    @Provides
    @Singleton
    fun provideDataStore(application: Application): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            application.preferencesDataStoreFile("prefs")
        }
    }



    private val cacheInterceptor = Interceptor { chain ->
        val response: Response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(30, TimeUnit.DAYS)
            .build()
        response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build()
    }


}