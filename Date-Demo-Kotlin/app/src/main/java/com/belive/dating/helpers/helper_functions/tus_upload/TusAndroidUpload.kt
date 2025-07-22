package com.belive.dating.helpers.helper_functions.tus_upload

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.belive.dating.extensions.logger
import io.tus.java.client.TusUpload
import java.io.FileNotFoundException
import java.io.IOException

class TusAndroidUpload(uri: Uri, context: Context) : TusUpload() {

    init {
        val resolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = resolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE, OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )
        if (cursor == null) {
            throw FileNotFoundException()
        }

        val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val name: String = cursor.getString(nameIndex)

        val fd: ParcelFileDescriptor = resolver.openFileDescriptor(uri, "r") ?: throw FileNotFoundException()
        val size: Long = try {
            fd.statSize
        } finally {
            try {
                fd.close()
            } catch (e: IOException) {
                logger("TusAndroidUpload", "unable to close ParcelFileDescriptor")
            }
        }

        setSize(size)
        setInputStream(resolver.openInputStream(uri))

        fingerprint = "$uri-$size"

        val metadata = HashMap<String, String>()
        metadata["filename"] = name
        setMetadata(metadata)

        cursor.close()
    }
}