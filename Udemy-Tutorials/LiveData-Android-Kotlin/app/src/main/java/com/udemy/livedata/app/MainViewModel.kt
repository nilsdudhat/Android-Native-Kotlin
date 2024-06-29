package com.udemy.livedata.app

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    val counter = MutableLiveData(0)

    fun increaseCounter(view: View) {
        counter.value = counter.value?.plus(1)
    }
}