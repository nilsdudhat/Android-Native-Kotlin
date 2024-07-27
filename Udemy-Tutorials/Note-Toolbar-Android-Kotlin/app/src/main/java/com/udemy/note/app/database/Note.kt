package com.udemy.note.app.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "note_table")
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    var title: String,
    var body: String,
) : Parcelable