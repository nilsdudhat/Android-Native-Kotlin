package com.udemy.retrofit.app.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JsonPlaceHolderInstance {

    companion object {
        @Volatile
        private var instance: Retrofit? = null
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

        fun getInstance(): Retrofit {
            if (instance == null) {
                instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                    .build()
            }
            return instance!!
        }
    }
}