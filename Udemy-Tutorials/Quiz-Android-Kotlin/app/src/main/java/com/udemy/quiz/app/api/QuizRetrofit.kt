package com.udemy.quiz.app.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuizRetrofit {

    companion object {
        private const val BASE_URL = "http://10.0.2.2/quiz/"
        private var INSTANCE: Retrofit? = null

        fun getInstance() : Retrofit {
            if (INSTANCE == null) {
                INSTANCE = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return INSTANCE!!
        }
    }
}