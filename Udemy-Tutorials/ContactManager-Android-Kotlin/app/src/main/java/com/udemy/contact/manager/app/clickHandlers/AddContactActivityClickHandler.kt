package com.udemy.contact.manager.app.clickHandlers

import android.app.Activity
import android.widget.Toast
import com.udemy.contact.manager.app.database.Contact
import com.udemy.contact.manager.app.database.ContactViewModel
import com.udemy.contact.manager.app.databinding.ActivityAddContactBinding
import com.udemy.contact.manager.app.hideKeyboard
import com.udemy.contact.manager.app.runInBackground

class AddContactActivityClickHandler(
    private val activity: Activity,
    private val binding: ActivityAddContactBinding,
) {

    fun onSaveClick(isUpdate: Boolean, contact: Contact, viewModel: ContactViewModel) {
        activity.hideKeyboard()

        if ((contact.name == null) || contact.name?.isEmpty() == true) {
            binding.edtName.error = "Name cannot be empty"
        } else if ((contact.email == null) || contact.email?.isEmpty() == true) {
            binding.edtEmail.error = "Email cannot be empty"
        } else {
            if (isUpdate) {
                viewModel.updateContact(contact).invokeOnCompletion {
                    activity.finish()
                }
            } else {
                runInBackground {
                    if (viewModel.isContactExists(contact) == 1) {
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Email already exists", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        viewModel.insertContact(contact).invokeOnCompletion {
                            activity.finish()
                        }
                    }
                }
            }
        }
    }
}