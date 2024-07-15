package com.udemy.quiz.app.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.udemy.quiz.app.R
import com.udemy.quiz.app.clicklisteners.OnNextClickListener
import com.udemy.quiz.app.databinding.ActivityMainBinding
import com.udemy.quiz.app.models.Question
import com.udemy.quiz.app.utils.ProgressUtil
import com.udemy.quiz.app.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var questionList: List<Question> = ArrayList()

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.nextClick = OnNextClickListener(this@MainActivity, binding, viewModel)

        ProgressUtil.showLoading(this)
        binding.isLoading = true

        viewModel.getQuestion().observe(this) {
            ProgressUtil.hideLoading()
            binding.isLoading = false

            if (it != null) {
                questionList = it
                viewModel.questionList = ArrayList(it)
            }

            binding.isDataAvailable = questionList.isNotEmpty()

            if (questionList.isNotEmpty()) {
                binding.question = questionList[viewModel.currentQuestionIndex]
            }
        }

        viewModel.getIsFinish().observe(this) {
            binding.isFinish = it
        }

        viewModel.getResult().observe(this) {
            binding.result = it
        }
    }
}