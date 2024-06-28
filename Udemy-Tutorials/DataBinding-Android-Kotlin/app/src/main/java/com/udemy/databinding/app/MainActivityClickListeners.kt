package com.udemy.databinding.app

import android.content.Context
import android.view.View
import android.widget.Toast

class MainActivityClickListeners(private val context: Context) {

    fun runMe(view: View) {
        Toast.makeText(context, "Button Clicked", Toast.LENGTH_SHORT).show()
    }
}