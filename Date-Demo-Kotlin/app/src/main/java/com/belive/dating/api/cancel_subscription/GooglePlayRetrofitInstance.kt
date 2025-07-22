package com.belive.dating.api.cancel_subscription

import android.app.Activity
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GooglePlayRetrofitInstance {
    fun createGooglePlayApiService(activity: Activity): GooglePlayApiService {
        val accessToken = getAccessToken(activity)

        val gson: Gson = GsonBuilder()
            .serializeNulls()
            .setStrictness(Strictness.LENIENT)
            .create()

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://androidpublisher.googleapis.com/androidpublisher/v3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(GooglePlayApiService::class.java)
    }

    private fun getAccessToken(activity: Activity): String? {
        val googleCredentials = GoogleCredentials
            .fromStream(activity.assets.open("service-account.json"))
            .createScoped(listOf("https://www.googleapis.com/auth/androidpublisher"))
        googleCredentials.refreshIfExpired()

        return googleCredentials.accessToken.tokenValue
    }
}