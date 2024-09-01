package com.practical.crawlapps.viewmodels

import android.app.Activity
import android.content.Intent
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.practical.crawlapps.activities.BoxActivity
import com.practical.crawlapps.databinding.ActivityMainBinding

data class MainFactory(val activity: Activity, val binding: ActivityMainBinding)

class MainViewModel(private val factory: MainFactory): ViewModel() {

    fun openBoxActivity(size: String) {
        if (size.isNotEmpty() && size.isDigitsOnly()) {
            val intent = Intent(factory.activity, BoxActivity::class.java)
            intent.putExtra("size", size.toInt())
            factory.activity.startActivity(intent)
        } else {
            factory.binding.inputSize.error = "Please enter valid number i.e. (2-9)"
        }
    }
}