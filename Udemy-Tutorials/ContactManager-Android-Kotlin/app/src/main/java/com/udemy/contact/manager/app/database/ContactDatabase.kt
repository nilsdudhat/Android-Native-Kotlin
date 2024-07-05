package com.udemy.contact.manager.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun getContactDao(): ContactDao

    /*
    * Singleton Design Pattern :
    * only one instance of the database exists,
    * to avoid unnecessary overhead associated with repeated database creation,
    * used to manage proper memory management
    *
    * companion object :
    * define a static singleton instance of this Database class
    *
    * @Volatile :
    * prevents  any possible race conditions in multithreading
    *
    * synchronised :
    * with synchronised block, only one thread can access the database at a time
    *
    * */

    companion object {
        @Volatile
        private var instance: ContactDatabase? = null

        fun getInstance(ctx: Context): ContactDatabase {
            synchronized(this) {
                if (instance == null)
                    instance = Room.databaseBuilder(
                        ctx.applicationContext, ContactDatabase::class.java,
                        "contacts_database"
                    ).build()

                return instance!!
            }
        }
    }
}