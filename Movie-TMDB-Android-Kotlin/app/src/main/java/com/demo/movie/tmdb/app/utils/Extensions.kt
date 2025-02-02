package com.demo.movie.tmdb.app.utils

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
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import org.koin.mp.KoinPlatform.getKoin
import java.util.Calendar
import java.util.Date

fun centerInConstraintLayout(parent: ConstraintLayout, view: View, matchView: View) {
    val constraintSet = ConstraintSet()
    constraintSet.clone(parent)

    constraintSet.clear(view.id, ConstraintSet.LEFT)
    constraintSet.clear(view.id, ConstraintSet.TOP)
    constraintSet.clear(view.id, ConstraintSet.RIGHT)
    constraintSet.clear(view.id, ConstraintSet.BOTTOM)

    // Center horizontally
    constraintSet.connect(
        view.id, ConstraintSet.START,
        matchView.id, ConstraintSet.START, 0
    )
    constraintSet.connect(
        view.id, ConstraintSet.END,
        matchView.id, ConstraintSet.END, 0
    )

    // Center vertically
    constraintSet.connect(
        view.id, ConstraintSet.TOP,
        matchView.id, ConstraintSet.TOP, 0
    )
    constraintSet.connect(
        view.id, ConstraintSet.BOTTOM,
        matchView.id, ConstraintSet.BOTTOM, 0
    )

    // Set horizontal and vertical bias to center
    /*constraintSet.setHorizontalBias(view.id, 0.5f)
    constraintSet.setVerticalBias(view.id, 0.5f)*/

    // Apply the constraints
    constraintSet.applyTo(parent)
}

fun centerInFrameLayout(parent: FrameLayout, view: View) {
    val layoutParams = view.layoutParams as FrameLayout.LayoutParams
    layoutParams.gravity = Gravity.CENTER
}

fun centerInLinearLayout(parent: LinearLayout, view: View) {
    val layoutParams = view.layoutParams as LinearLayout.LayoutParams
    layoutParams.gravity = Gravity.CENTER
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
fun isWebUrl(url: String): Boolean {
    return Patterns.WEB_URL.matcher(url).matches()
}

fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
/*------------------------------------------------------------------------------------------------*/
fun <T : ViewModel> T.createFactory(): ViewModelProvider.Factory {
    val viewModel = this
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModel as T
    }
}
/*------------------------------------------------------------------------------------------------*/
@Navigator.Name("keep_state_fragment") // 'keep_state_fragment' is used in navigation/nav_graph.xml
class KeepStateNavigator(
    context: Context,
    manager: FragmentManager, // MUST pass childFragmentManager.
    containerId: Int
) : FragmentNavigator(context, manager, containerId)

/*------------------------------------------------------------------------------------------------*/
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T?>) {
    observe(lifecycleOwner, object : Observer<T?> {
        override fun onChanged(value: T?) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}
/*------------------------------------------------------------------------------------------------*/
fun Context.toast(message: CharSequence, toastLength: Int) =
    Toast.makeText(this, message, toastLength).show()
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
enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
}
/*------------------------------------------------------------------------------------------------*/
fun getGlideManager(): RequestManager {
    return getKoin().get<RequestManager>()
}

fun getKoinContext(): Context {
    return getKoin().get<Context>()
}
/*------------------------------------------------------------------------------------------------*/
data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}
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

fun Activity.showSnackBar(
    message: String,
    view: View? = null,
    action: String? = "OK",
    actionListener: View.OnClickListener? = null
) {
    Snackbar
        .make(
            view ?: this.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG,
        )
        .setAction(action) {
            actionListener?.onClick(it)
        }
        .setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        .show()
}

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