package com.demo.api.app

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.Date

fun <T> Call<T>.enqueue(callback: CallBackKt<T>.() -> Unit) {
    val callBackKt = CallBackKt<T>()
    callback.invoke(callBackKt)
    this.enqueue(callBackKt)
}

class CallBackKt<T> : Callback<T> {

    private var onResponse: ((Response<T>) -> Unit)? = null
    private var onFailure: ((t: Throwable?) -> Unit)? = null

    override fun onFailure(call: Call<T>, t: Throwable) {
        onFailure?.invoke(t)
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        onResponse?.invoke(response)
    }
}

private val httpClient = OkHttpClient.Builder()
private val retrofitBuilder = Retrofit.Builder()
private val gsonConverterFactory = GsonConverterFactory.create()

fun getClient(baseUrl: String): Retrofit {

    httpClient.addInterceptor { chain ->
        val request: Request =
            chain.request().newBuilder()
                .build()
        chain.proceed(request)
    }

    return retrofitBuilder.baseUrl(baseUrl)
        .addConverterFactory(gsonConverterFactory)
        .client(httpClient.build())
        .build()
}

/*------------------------------------------------------------------------------------------------*/
fun getTimeAgo(date: Date): String {
    val timeAgo: String = DateUtils.getRelativeTimeSpanString(
        date.time,
        Calendar.getInstance().timeInMillis,
        DateUtils.MINUTE_IN_MILLIS
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModel as T
    }
}
/*------------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------------*/
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T?>) {
    observe(lifecycleOwner, object : Observer<T?> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
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

/*------------------------------------------------------------------------------------------------*/
fun Activity.changeStatusBarColor(color: Int, isLight: Boolean) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
}

/*------------------------------------------------------------------------------------------------*/
fun View.asString(): String {
    return when (this) {
        is TextView -> text.toString().trim().ifNotNullOrElse({ it }, { "" })
        else -> ""
    }
}

inline fun <T : Any, R> T?.ifNotNullOrElse(ifNotNullPath: (T) -> R, elsePath: () -> R) =
    let { if (it == null) elsePath() else ifNotNullPath(it) }
/*------------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------------*/
fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

class SafeClickListener(
    private var defaultInterval: Int = 800,
    private val onSafeCLick: (View) -> Unit,
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}
/*------------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------------------------*/
fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}
/*------------------------------------------------------------------------------------------------*/