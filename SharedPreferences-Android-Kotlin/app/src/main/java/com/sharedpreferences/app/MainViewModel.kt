package com.sharedpreferences.app

import android.content.Context
import androidx.lifecycle.ViewModel
import com.sharedpreferences.app.databinding.ActivityMainBinding

data class MainFactory(val context: Context, val binding: ActivityMainBinding)

class MainViewModel(private val factory: MainFactory): ViewModel() {

//    private val prefs = PrefUtils(factory.context, "demo")
    private val prefs = EncryptedPrefUtils(factory.context, "demo")

    fun saveValue() {
        prefs.setString("pref", factory.binding.edtValue.text.toString())
    }

    fun getValue() {
        factory.binding.value = prefs.getString("pref")
    }
}