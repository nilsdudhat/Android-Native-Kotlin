package com.udemy.contact.manager.app.database

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.udemy.contact.manager.app.R
import com.udemy.contact.manager.app.commonDialog
import com.udemy.contact.manager.app.databinding.DialogDeleteBinding
import kotlinx.coroutines.launch

/**
 * AndroidViewModel -
 * it is a subclass of ViewModel and similar to it,
 * both are designed to store and manage UI related data
 * and are responsible to prepare & provide data for UI
 * and automatically allow data to survive configuration changes.
 */
class ContactViewModel(private val application: Application) : AndroidViewModel(application) {

    /*
    * If you need to use context inside ViewModel,
    * you have to use AndroidViewModel, because it provides access to the context
    */

    private val repository: ContactRepository = ContactRepository(application)

    private var contactList: ArrayList<Contact> = ArrayList()

    fun insertContact(contact: Contact) = viewModelScope.launch {
        repository.insertContact(contact)
    }

    fun updateContact(contact: Contact) = viewModelScope.launch {
        repository.updateContact(contact)
    }

    fun deleteContact(activity: AppCompatActivity, contact: Contact) {
        activity.commonDialog(R.layout.dialog_delete, dialogBuilder = {
            val binding = DialogDeleteBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.contact = contact

            binding.apply {
                btnYes.setOnClickListener {
                    viewModelScope.launch {
                        repository.deleteContact(contact)
                    }
                    dismiss()
                }
                btnNo.setOnClickListener {
                    dismiss()
                }
            }
        })
    }

    fun isContactExists(contact: Contact): Int {
        return repository.isContactExists(contact)
    }

    fun getAllContacts(): LiveData<List<Contact>> {
        repository.getAllContacts().observeForever(Observer {
            contactList = ArrayList(it)
        })

        return repository.getAllContacts()
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}