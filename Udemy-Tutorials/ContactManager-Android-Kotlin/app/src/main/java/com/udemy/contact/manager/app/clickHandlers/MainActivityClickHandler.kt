package com.udemy.contact.manager.app.clickHandlers

import android.content.Context
import android.content.Intent
import android.view.View
import com.udemy.contact.manager.app.activities.AddContactActivity

class MainActivityClickHandler(private val context: Context) {

    fun onAddContactClick(view: View) {
        val intent = Intent(context, AddContactActivity::class.java)
        context.startActivity(intent)
    }
}