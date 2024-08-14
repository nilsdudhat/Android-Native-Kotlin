package com.demo.gallery.app.room

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
@Entity(tableName = "media_table")
data class Media(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val fileID: String,
    val path: String,
    val dateModified: String,
    val fileFormat: String,
    val size: String,
) : Parcelable, Serializable