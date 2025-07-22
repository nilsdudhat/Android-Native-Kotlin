package com.belive.dating.extensions

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.core.graphics.scale
import androidx.core.net.toUri
import java.io.File
import java.io.FileNotFoundException

/*fun getRealPathFromUri(uri: Uri): String? {
    if (DocumentsContract.isDocumentUri(getKoinContext(), uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        when (uri.authority) {
            "com.android.providers.media.documents" -> {
                val split = docId.split(":")
                val type = split[0]
                val contentUri = when (type) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    else -> null
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(getKoinContext(), contentUri, selection, selectionArgs)
            }
            "com.android.providers.downloads.documents" -> {
                val contentUri = ContentUris.withAppendedId(
                    "content://downloads/public_downloads".toUri(),
                    docId.toLongOrNull() ?: return null
                )
                return getDataColumn(getKoinContext(), contentUri, null, null)
            }
        }
    } else if (uri.scheme.equals("content", true)) {
        return getDataColumn(getKoinContext(), uri, null, null)
    } else if (uri.scheme.equals("file", true)) {
        return uri.path
    }
    return null
}

fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
    uri ?: return null
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            return cursor.getString(columnIndex)
        }
    }
    return null
}*/

fun Context.getRealPathFromUri(uri: Uri): String? {
    // Check if the Uri is a content Uri
    if ("content".equals(uri.scheme, ignoreCase = true)) {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                val displayName = cursor.getString(nameIndex)

                // sanitize the displayName
                val fileName = sanitizeFilename(displayName)

                // Create a temporary file in the app's cache directory
                val tempFile = File(cacheDir, fileName)
                tempFile.deleteOnExit() // Delete the file when the VM exits

                // Copy the content of the Uri to the temporary file
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        tempFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                } catch (e: Exception) {
                    catchLog("getRealPathFromUri: ${gsonString(e)}")
                }
                // Return the absolute path of the temporary file
                return tempFile.absolutePath
            }
        }
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        // If the Uri is a file Uri, directly return the path
        return uri.path
    }
    return null // Return null if unable to get the real path
}

fun sanitizeFilename(displayName: String): String {
    val badCharacters = arrayOf("..", "/")
    val segments = displayName.split("/")
    var fileName = segments[segments.size - 1]
    for (unsafeCharacter in badCharacters) {
        fileName = fileName.replace(unsafeCharacter, "_")
    }
    return fileName
}

fun isNetworkPath(path: String): Boolean {
    val uri = path.toUri()
    return uri.scheme == "http" || uri.scheme == "https" || uri.scheme == "ftp"
}

fun Context.resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    return bitmap.scale(maxWidth, maxHeight, false)
}

fun Context.getVideoDuration(videoUri: Uri): Long {
    val retriever = MediaMetadataRetriever()
    var pfd: ParcelFileDescriptor? = null
    try {
        pfd = contentResolver.openFileDescriptor(videoUri, "r")
        pfd?.fileDescriptor?.let {
            retriever.setDataSource(it)
            val durationString =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationString?.toLong() ?: 0L
        }
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        retriever.release()
        pfd?.close()
    }
    return 0L
}