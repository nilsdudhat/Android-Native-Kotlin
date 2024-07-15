package com.udemy.quiz.app.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udemy.quiz.app.api.QuizAPI
import com.udemy.quiz.app.api.QuizRetrofit
import com.udemy.quiz.app.models.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuizRepository {

    private var questionsLiveData = MutableLiveData<List<Question>?>()

    private val quizAPI = QuizRetrofit.getInstance().create(QuizAPI::class.java)

    fun getQuestions(): LiveData<List<Question>?> {
        if (questionsLiveData.value == null) {
            CoroutineScope(Dispatchers.IO).launch {
                /*quizAPI.getQuestions().enqueue(object : Callback<List<Question>> {
                    override fun onResponse(
                        call: Call<List<Question>>,
                        response: Response<List<Question>>,
                    ) {
                        if (response.isSuccessful) {
                            questionsLiveData.value = response.body()
                        }
                    }

                    override fun onFailure(call: Call<List<Question>>, throwable: Throwable) {

                    }
                })*/

                val response = quizAPI.getQuestions()

                if (response.isSuccessful) {
                    /*
                    * postValue -> can be executed with background thread as well
                    * setValue -> can be executed with main thread only
                    * */
                    questionsLiveData.postValue(response.body())
                }
            }
        }
        return questionsLiveData
    }
}