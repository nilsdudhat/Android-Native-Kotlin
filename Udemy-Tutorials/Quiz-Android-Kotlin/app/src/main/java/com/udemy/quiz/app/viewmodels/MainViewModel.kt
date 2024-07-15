package com.udemy.quiz.app.viewmodels

import android.content.Intent
import android.widget.RadioButton
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udemy.quiz.app.activities.MainActivity
import com.udemy.quiz.app.activities.ResultActivity
import com.udemy.quiz.app.databinding.ActivityMainBinding
import com.udemy.quiz.app.models.Question
import com.udemy.quiz.app.repository.QuizRepository

class MainViewModel : ViewModel() {

    private val repository = QuizRepository()

    var currentQuestionIndex = 0

    private val isFinishLiveData = MutableLiveData(false)
    private val resultLiveData = MutableLiveData("")

    private val answerMap = mutableMapOf<Int, Boolean>()

    var questionList = ArrayList<Question>()

    fun getResult(): LiveData<String> {
        return resultLiveData
    }

    fun getIsFinish(): LiveData<Boolean> {
        return isFinishLiveData
    }

    fun getQuestion(): LiveData<List<Question>?> {
        return repository.getQuestions()
    }

    fun onNextClick(activity: MainActivity, binding: ActivityMainBinding) {
        val id = binding.radioOptions.checkedRadioButtonId
        val selectedOption = binding.radioOptions.findViewById<RadioButton>(id)
        if (selectedOption == null) {
            Toast.makeText(activity, "Please select an option", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedAnswer = selectedOption.text.toString()
        binding.radioOptions.clearCheck()

        if (currentQuestionIndex >= 0) {
            answerMap[currentQuestionIndex] =
                selectedAnswer == questionList[currentQuestionIndex].correctOption
        }
        currentQuestionIndex++

        var correctAnswers = 0
        for (i in answerMap.keys) {
            val answer = answerMap[i]

            if (answer!!) {
                correctAnswers = correctAnswers.plus(1)
            }
        }

        resultLiveData.postValue("Correct Answers: $correctAnswers out of ${questionList.size}")

        if (currentQuestionIndex >= (questionList.size - 1)) {
            isFinishLiveData.postValue(currentQuestionIndex >= questionList.size - 1)
        }
        if (currentQuestionIndex < questionList.size) {
            binding.question = questionList[currentQuestionIndex]
        }
        if (currentQuestionIndex == questionList.size) {
            val intent = Intent(activity, ResultActivity::class.java)
            intent.putExtra("result", resultLiveData.value)
            activity.startActivity(intent)
            activity.finish()
        }
    }
}