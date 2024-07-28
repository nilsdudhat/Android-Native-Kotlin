package com.demo.movie.tmdb.app.api

import com.demo.movie.tmdb.app.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIClient {

    companion object {
        private var instance: Retrofit? = null

        fun getInstance(): Retrofit {

            if (instance == null) {
                val httpClient = OkHttpClient.Builder()

                httpClient.addInterceptor { chain ->
                    val request: Request =
                        chain.request().newBuilder()
                            .addHeader("accept", "application/json")
                            .addHeader("Authorization", "Bearer ${Constants.API_TOKEN}")
                            .build()
                    chain.proceed(request)
                }

                instance = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build()
            }
            return instance!!
        }
    }
}