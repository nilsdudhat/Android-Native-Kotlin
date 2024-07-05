package com.udemy.contact.manager.app.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Dao: Data Access Object
 * */
@Dao
interface ContactDao {

    /*
    * suspend : it is used before fun keyword for coroutines,
    * it will run in background thread
    * */

    @Insert
    suspend fun insertContact(contact: Contact): Long

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT EXISTS(SELECT * FROM contacts_table WHERE contact_email = :email)")
    fun isContactExist(email: String): Int

    @Query("SELECT * FROM contacts_table")
    fun getAllContacts(): LiveData<List<Contact>>

    @Query("DELETE FROM contacts_table")
    suspend fun deleteAll()
}