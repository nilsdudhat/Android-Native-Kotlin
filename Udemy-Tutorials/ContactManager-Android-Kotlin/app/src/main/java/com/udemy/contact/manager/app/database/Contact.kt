package com.udemy.contact.manager.app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "contacts_table")
data class Contact(

    @ColumnInfo(name = "contact_id")
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    @ColumnInfo(name = "contact_name")
    var name: String? = null,

    @ColumnInfo(name = "contact_email")
    var email: String? = null,
) : Serializable