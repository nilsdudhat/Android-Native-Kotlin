package com.demo.gallery.app.utils

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import com.demo.gallery.app.room.Media

private const val BASE_SELECTION = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        + " OR "
        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

private val BASE_PROJECTION: Array<String> = arrayOf(
    MediaStore.Files.FileColumns._ID,  // file id
    MediaStore.Files.FileColumns.DATA,  // file path
    MediaStore.Files.FileColumns.DATE_MODIFIED,  // date modified
    MediaStore.Files.FileColumns.MIME_TYPE,  // full format of file
    MediaStore.Files.FileColumns.SIZE, // full format of file
)

fun getAllMedia(context: Context): ArrayList<Media> {
    val cursor = makeMediaCursor(context, null, null)
    return getAllMedia(cursor)
}

private fun makeMediaCursor(
    context: Context,
    selection: String?,
    selectionValues: Array<String?>?
): Cursor? {
    return makeMediaCursor(
        context,
        selection,
        selectionValues,
        MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC",
    )
}

fun makeMediaCursor(
    context: Context,
    selection: String?,
    selectionValues: Array<String?>?,
    sortOrder: String?,
): Cursor? {
    var newSelection = selection
    newSelection = if (newSelection != null && newSelection.trim { it <= ' ' } != "") {
        "$BASE_SELECTION AND $newSelection"
    } else {
        BASE_SELECTION
    }

    return try {
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            BASE_PROJECTION,
            newSelection,
            selectionValues,
            sortOrder,
        )
    } catch (e: SecurityException) {
        null
    }
}

private fun getAllMedia(cursor: Cursor?): ArrayList<Media> {
    val mediaList = ArrayList<Media>()

    try {
        if ((cursor != null) && cursor.moveToFirst()) {

            val columnId = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val columnPath =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val columnDateModified =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val columnFileFormat =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val columnFileSize =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            do {
                val id = cursor.getString(columnId)
                val path = cursor.getString(columnPath)
                val dateModified: String = getFullDateFromLong(
                    cursor.getString(columnDateModified).toLong()
                )
                val fileFormat = cursor.getString(columnFileFormat)
                val size = cursor.getString(columnFileSize)

                val media = Media(
                    fileID = id,
                    path = path,
                    dateModified = dateModified,
                    fileFormat = fileFormat,
                    size = size,
                )

                mediaList.add(media)

                Log.d("--path--", "getMedias: " + media.path)
            } while (cursor.moveToNext())
        }
    } catch (error: Exception) {
        Log.d("--catch--", "getAllMedia: ${error.message}")
    }
    Log.d("--media--", "getAllMedia: ${mediaList.size}")
    
    return mediaList
}