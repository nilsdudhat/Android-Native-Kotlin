package com.belive.dating.api.gist

import android.content.Context
import com.belive.dating.R
import com.belive.dating.extensions.logger
import com.belive.dating.helpers.helper_functions.aes.AESUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GistClient {

    fun getGistInstance(context: Context): Retrofit {
        val gson: Gson = GsonBuilder()
            .serializeNulls()
            .setStrictness(Strictness.LENIENT)
            .create()

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS) // Increase connect timeout
            .readTimeout(15, TimeUnit.SECONDS)    // Increase read timeout
            .writeTimeout(0, TimeUnit.SECONDS)   // Increase write timeout
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            ).addInterceptor { chain ->
                val request: Request = chain.request().newBuilder().build()
                chain.proceed(request)
            }

        val gistUrl = AESUtils.Logd(context.resources.getString(R.string.json_required))
        logger("--gist--", "gistUrl: $gistUrl")

        return Retrofit.Builder()
            .baseUrl(gistUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())
            .build()
    }
}