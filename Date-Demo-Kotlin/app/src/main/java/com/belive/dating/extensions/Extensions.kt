package com.belive.dating.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Parcelable
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.LocaleSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.belive.dating.BuildConfig
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.api.introduction.models.InterestsResponse
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.parcelize.Parcelize
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Locale

fun isAppUpdateRequired(): Boolean {
    if (getGistPrefs().appVersionCode.isNotEmpty()) {
        return BuildConfig.VERSION_CODE < getGistPrefs().appVersionCode.toInt()
    }
    return false
}

fun Context.isAppInForeground(): Boolean {
    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val runningAppProcesses = activityManager.runningAppProcesses ?: return false
    return runningAppProcesses.any { it.processName == packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
}

fun clearAppData(context: Context) {
    try {
        val files = arrayOf(
            context.cacheDir,
            context.filesDir,
            context.externalCacheDir,
            File(context.applicationInfo.dataDir, "shared_prefs"),
            File(context.applicationInfo.dataDir, "databases"),
        )
        for (dir in files) {
            dir?.deleteRecursively()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Map<String, Any>.toJsonObject(): JsonObject {
    val jsonObject = JsonObject()
    for ((key, value) in this) {
        when (value) {
            is Number -> jsonObject.addProperty(key, value)
            is String -> jsonObject.addProperty(key, value)
            is Boolean -> jsonObject.addProperty(key, value)
            is Char -> jsonObject.addProperty(key, value)
            else -> jsonObject.add(key, JsonPrimitive(value.toString())) // Default fallback
        }
    }
    return jsonObject
}

fun getBaseUrl(url: String): String {
    return try {
        val uri = URL(url)
        "${uri.protocol}://${uri.host}"
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        ""
    }
}

@SuppressLint("HardwareIds")
fun Context.getDeviceID(): String? {
    return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
}

fun Activity.isOnlyActivity(): Boolean {
    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val tasks = activityManager.appTasks
    return tasks.isNotEmpty() && tasks[0].taskInfo.numActivities == 1
}

fun Activity.openImageInPlayer(uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(intent)
}

fun Activity.openVideoInPlayer(uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "video/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(intent)
}

fun gsonString(any: Any?): String {
    return Gson().toJson(any)
}

inline fun <reified T> String.fromJson(): T? {
    return try {
        val type = object : TypeToken<T>() {}.type
        Gson().fromJson<T>(this, type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

inline fun <reified T> String.fromJson(typeToken: TypeToken<T>): T? {
    return try {
        Gson().fromJson<T>(this, typeToken.type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun formatStringsForWidth(
    startString: String,
    endString: String,
    textView: TextView,
    maxWidth: Int,
    onSpannableAvailable: (SpannableString) -> Unit,
) {
    // Combine name and age
    val fullText = "$startString $endString".trim()

    // Create a SpannableString
    val spannable = SpannableString(fullText)

    // Apply bold style to the name
    spannable.setSpan(
        StyleSpan(Typeface.BOLD), 0, startString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Set the ellipsis if the text exceeds maxWidth
    textView.post {
        // Measure the full text width
        val paint = textView.paint
        val fullTextWidth = paint.measureText(fullText)

        // Check if it exceeds the max width
        if (fullTextWidth > maxWidth) {
            // Find the max length of name that fits within maxWidth - endString width
            val ageWidth = paint.measureText(" $endString")
            val availableWidth = maxWidth - ageWidth

            // Truncate the name and append ellipsis
            val truncatedName = TextUtils.ellipsize(
                startString, paint, availableWidth, TextUtils.TruncateAt.END
            ).toString()

            // Update the spannable with truncated name
            val newFullText = "$truncatedName $endString".trim()
            val updatedSpannable = SpannableString(newFullText)

            updatedSpannable.setSpan(
                StyleSpan(Typeface.BOLD), 0, truncatedName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Force LTR layout direction
            updatedSpannable.setSpan(LocaleSpan(Locale.ENGLISH), 0, updatedSpannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            onSpannableAvailable.invoke(updatedSpannable)
        } else {
            // Force LTR layout direction
            spannable.setSpan(LocaleSpan(Locale.ENGLISH), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            onSpannableAvailable.invoke(spannable)
        }
    }
}

fun <T : ViewModel> T.createFactory(): ViewModelProvider.Factory {
    val viewModel = this
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModel as T
    }
}

fun Activity.reOpenApp() {
    val intent = Intent(this, MainActivity::class.java)
    intent.putExtra("display_splash", false)
    val pendingIntent =
        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    try {
        pendingIntent.send()
    } catch (e: PendingIntent.CanceledException) {
        // Handle the exception
    }
    finish()
}

fun getMediaType(): MediaType? {
    return "text/plain".toMediaTypeOrNull()
}

fun prepareImagePart(name: String, filePath: String): MultipartBody.Part {
    val file = File(filePath)
    val requestBody = file.asRequestBody("image/webp".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(name, file.name, requestBody)
}

fun prepareSelfiePart(filePath: String): MultipartBody.Part {
    val file = File(filePath)
    val requestBody = file.asRequestBody("image/webp".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("selfie", file.name, requestBody)
}

fun Context.openBrowser(url: String) {
    var customURL = url
    if (!customURL.startsWith("http://") && !customURL.startsWith("https://")) {
        customURL = "https://$customURL"
    }
    val intent = Intent(Intent.ACTION_VIEW, customURL.toUri())
    startActivity(
        Intent.createChooser(
            intent,
            "Choose Browser",
        ).setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    )
}

fun getErrorMessage(errorBody: String?): String {
    return try {
        if (!errorBody.isNullOrEmpty()) {
            val jsonObject = JSONObject(errorBody)
            jsonObject.getString("message")
        } else {
            "Something went wrong...!"
        }
    } catch (e: Exception) {
        "Something went wrong...!"
    }
}

fun getBitmapFromLocalPath(localPath: String): Bitmap? {
    return BitmapFactory.decodeFile(localPath)
}

fun getBitmapFromUrl(urlString: String): Bitmap? {
    return try {
        val url = URL(urlString)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Parcelize
enum class Status : Parcelable {
    LOADING, ADMIN_BLOCKED, SIGN_OUT, ERROR, SUCCESS,
}

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> signOut(msg: String, data: T?): Resource<T> {
            return Resource(Status.SIGN_OUT, data, msg)
        }

        fun <T> adminBlocked(msg: String, data: T?): Resource<T> {
            return Resource(Status.ADMIN_BLOCKED, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}
