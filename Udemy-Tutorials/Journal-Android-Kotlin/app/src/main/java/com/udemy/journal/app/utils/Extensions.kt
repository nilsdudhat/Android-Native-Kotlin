package com.udemy.journal.app.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateUtils
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date

/*------------------------------------------------------------------------------------------------*/
fun getTimeAgo(date: Date): String {
    val timeAgo: String = DateUtils.getRelativeTimeSpanString(
        date.time,
        Calendar.getInstance().timeInMillis,
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
    return timeAgo
}

fun getTimeAgo(timeStamp: Timestamp): String {
    val timeAgo: String = DateUtils.getRelativeTimeSpanString(
        (timeStamp.seconds * 1000),
    ).toString()
    return timeAgo
}

fun getTimeAgo(timeStamp: java.sql.Timestamp): String {
    val timeAgo: String = DateUtils.getRelativeTimeSpanString(
        (timeStamp.nanos / 1000).toLong(),
    ).toString()
    return timeAgo
}
/*------------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------------*/
fun isWebUrl(url: String): Boolean {
    return Patterns.WEB_URL.matcher(url).matches()
}

fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
/*------------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------------*/
fun <T : ViewModel> T.createFactory(): ViewModelProvider.Factory {
    val viewModel = this
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModel as T
    }
}
/*------------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------------*/
fun Context.toast(message: CharSequence, toastLength: Int) =
    Toast.makeText(this, message, toastLength).show()
/*------------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------------*/
fun View.showKeyboard() {
    this.requestFocus()
    val inputMethodManager =
        context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
    this.clearFocus()
    val inputMethodManager =
        context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Activity.showKeyboard() {
    val view = this.currentFocus
    val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
}
/*------------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------------*/
internal fun Activity.commonDialog(layoutResourceId: Int, dialogBuilder: Dialog.() -> Unit) {
    Dialog(this).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(layoutResourceId)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setWidth(0.85f)
        dialogBuilder()
        dismiss()
        if (!isShowing) {
            show()
        }
    }
}

internal fun Dialog.setWidth(width: Float = 0.9f) {
    this.window?.setLayout(
        (Resources.getSystem().displayMetrics.widthPixels * width).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
/*------------------------------------------------------------------------------------------------*/