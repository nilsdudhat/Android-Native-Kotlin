package com.belive.dating.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

private val TAG = "ImageProcessor"

fun Context.processImage(sourceFile: File): File? {
    return try {
        val bitmap = decodeImage(sourceFile) ?: return null

        // Rotate image if needed based on EXIF data
        val rotatedBitmap = rotateImage(bitmap, sourceFile)

        // Compress image if size exceeds 5MB
        val compressedBitmap = compressImage(rotatedBitmap)

        // Convert to WebP format
        val destinationFile =
            File(cacheDir, "/upload/images/webp/" + "${sourceFile.nameWithoutExtension}.webp")
        val webpPath = saveImageAsWebp(compressedBitmap, destinationFile)

        webpPath
    } catch (e: Exception) {
        logger(TAG, "Error processing image: ${e.message}")
        null
    }
}

private fun decodeImage(sourceFile: File): Bitmap? {
    try {
        logger("--compress--", "decodeImage: ${getFileSize(sourceFile)}")

        val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
        return bitmap
    } catch (e: Exception) {
        logger(TAG, "Error decoding image: ${e.message}")
        return null
    }
}

fun rotateImage(bitmap: Bitmap, sourceFile: File): Bitmap {
    try {
        val exifInterface = ExifInterface(sourceFile)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED,
        )

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        logger("--compress--", "rotateImage: ${getBitmapSize(rotatedBitmap)}")

        return rotatedBitmap
    } catch (e: Exception) {
        logger(TAG, "Error rotating image: ${e.message}")
        return bitmap
    }
}

private fun compressImage(bitmap: Bitmap): Bitmap {
    val maxSize = 5 * 1024 * 1024 // 5MB in bytes
    var quality = 100 // Start with the highest quality
    val outputStream = ByteArrayOutputStream()

    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

    while ((outputStream.toByteArray().size > maxSize) && (quality >= 20)) {
        outputStream.reset()
        quality -= 20 // Reduce quality step by step
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        logger(
            "--compress--",
            "compressImage: Quality: $quality, Size: ${
                getFileSize(outputStream.toByteArray().size.toLong())
            }"
        )
    }

    val compressedBytes = outputStream.toByteArray()
    return BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
}

private fun saveImageAsWebp(bitmap: Bitmap, destinationFile: File): File? {
    // Create a temporary file to store the compressed image
    if (!destinationFile.exists()) {
        destinationFile.parentFile?.mkdirs()
    } else {
        destinationFile.delete()
    }

    try {
        val outputStream = FileOutputStream(destinationFile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, outputStream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)
        }
        outputStream.flush()
        outputStream.close()

        logger("--compress--", "saveImageAsWebp: ${getFileSize(destinationFile.absoluteFile)}")
        logger("--compress--", "saveImageAsWebp: ${destinationFile.absoluteFile}")

        return destinationFile
    } catch (e: Exception) {
        logger(TAG, "Error saving image as WebP: ${e.message}")
        return null
    }
}

private fun getFileSize(file: File): String {
    val fileSizeInBytes = file.length() // Get file size in bytes
    val fileSizeInKB = fileSizeInBytes / 1024.0
    val fileSizeInMB = fileSizeInKB / 1024.0
    val fileSizeInGB = fileSizeInMB / 1024.0

    return when {
        fileSizeInGB >= 1 -> String.format(Locale.getDefault(), "%.2f GB", fileSizeInGB)
        fileSizeInMB >= 1 -> String.format(Locale.getDefault(), "%.2f MB", fileSizeInMB)
        fileSizeInKB >= 1 -> String.format(Locale.getDefault(), "%.2f KB", fileSizeInKB)
        else -> "$fileSizeInBytes Bytes"
    }
}

fun getFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    return String.format(
        Locale.getDefault(),
        "%.2f %s",
        bytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}

fun getBitmapSize(bitmap: Bitmap): String {
    val byteCount = bitmap.byteCount.toLong() // Get the byte size of the Bitmap
    return getFileSize(byteCount)
}

fun Context.validateResolutionOfImage(imagePath: String?): Boolean {
    try {
        if (imagePath == null) {
            return false
        }

        // Get InputStream from imagePath
        val inputStream: InputStream? =
            contentResolver.openInputStream(Uri.fromFile(File(imagePath)))

        // Decode image dimensions without loading the entire bitmap
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)

        var isValid = false
        if ((options.outWidth > 400) && (options.outHeight > 400)) {
            isValid = true
        }
        inputStream?.close()

        return isValid
    } catch (e: Exception) {
        catchLog("validateResolutionOfImage: " + gsonString(e))
        return false
    }
}