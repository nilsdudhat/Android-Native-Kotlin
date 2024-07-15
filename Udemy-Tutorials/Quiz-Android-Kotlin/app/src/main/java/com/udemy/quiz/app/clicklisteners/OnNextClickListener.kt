package com.udemy.quiz.app.clicklisteners

import com.udemy.quiz.app.activities.MainActivity
import com.udemy.quiz.app.databinding.ActivityMainBinding
import com.udemy.quiz.app.viewmodels.MainViewModel

class OnNextClickListener(
    val activity: MainActivity,
    val binding: ActivityMainBinding,
    val viewModel: MainViewModel,
) {
    fun onClick() {
        viewModel.onNextClick(activity, binding)
    }
}