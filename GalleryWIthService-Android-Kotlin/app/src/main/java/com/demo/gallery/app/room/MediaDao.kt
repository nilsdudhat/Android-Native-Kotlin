package com.demo.gallery.app.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MediaDao {

    @Insert
    suspend fun insert(media: Media)

    @Update
    suspend fun update(media: Media)

    @Delete
    suspend fun delete(media: Media)

    @Query("SELECT EXISTS(SELECT * FROM media_table WHERE path = :path)")
    suspend fun isMediaExistByPath(path: String): Boolean

    @Query("SELECT * FROM media_table WHERE path = :path")
    suspend fun getMediaByPath(path: String): Media

    @Query("DELETE FROM media_table")
    suspend fun deleteAllMedia()

    @Query("SELECT * FROM media_table ORDER BY dateAdded DESC")
    fun getAllMedia(): LiveData<List<Media>>
}