package com.udemy.quiz.app.api

import com.udemy.quiz.app.models.Question
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface QuizAPI {

    @GET("my_quiz_api.php")
    suspend fun getQuestions(): Response<List<Question>>

}