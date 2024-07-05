package com.udemy.contact.manager.app.clickHandlers

import android.content.Intent
import android.view.View
import com.udemy.contact.manager.app.activities.AddContactActivity
import com.udemy.contact.manager.app.database.Contact

class ContactClickListener {

    fun onContactClick(view: View, contact: Contact) {
        val intent = Intent(view.context, AddContactActivity::class.java)
        intent.putExtra("contact", contact)
        view.context.startActivity(intent)
    }
}