package com.belive.dating.api.ads_settings

import com.belive.dating.extensions.getGistPrefs
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AdsSettingClient {

    fun getAdsSettingInstance(): Retrofit {
        val gson: Gson = GsonBuilder()
            .serializeNulls()
            .setStrictness(Strictness.LENIENT)
            .create()

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Increase connect timeout
            .readTimeout(30, TimeUnit.SECONDS)    // Increase read timeout
            .writeTimeout(0, TimeUnit.SECONDS)   // Increase write timeout
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            ).addInterceptor { chain ->
                val request: Request = chain.request().newBuilder().build()
                chain.proceed(request)
            }

        return Retrofit.Builder()
            .baseUrl(getGistPrefs().baseURL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())
            .build()
    }
}