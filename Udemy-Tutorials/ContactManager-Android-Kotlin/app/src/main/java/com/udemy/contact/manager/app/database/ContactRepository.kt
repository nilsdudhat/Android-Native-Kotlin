package com.udemy.contact.manager.app.database

import android.app.Application
import androidx.lifecycle.LiveData

/**
 * Repository : acts as a bridge between the ViewModel and the Data Source.
 * */
class ContactRepository(application: Application) {

    private val contactDao: ContactDao

    init {
        val contactDatabase = ContactDatabase.getInstance(application)

        contactDao = contactDatabase.getContactDao()
    }

    suspend fun insertContact(contact: Contact): Long {
        return contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: Contact) {
        return contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: Contact) {
        return contactDao.deleteContact(contact)
    }

    fun isContactExists(contact: Contact): Int {
        return contactDao.isContactExist(contact.email!!)
    }

    fun getAllContacts(): LiveData<List<Contact>> {
        return contactDao.getAllContacts()
    }

    suspend fun deleteAll() {
        return contactDao.deleteAll()
    }
}