package com.belive.dating.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat

fun Context.getDimensionPixelOffset(@DimenRes dimenRes: Int): Int {
    return resources.getDimensionPixelOffset(dimenRes)
}

@ColorInt
fun Context.getColorFromAttr(@AttrRes attrColor: Int): Int {
    val typedValue = TypedValue()
    val theme = theme
    theme.resolveAttribute(attrColor, typedValue, true)
    return if (typedValue.resourceId != 0) {
        ContextCompat.getColor(this, typedValue.resourceId)
    } else {
        typedValue.data // For direct color values
    }
}

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}

fun Activity.getStatusBarHeight(): Int? {
    try {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsets = window.decorView.rootWindowInsets
            windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        } else {
            val rect = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rect)
            rect.top
        }
    } catch (e: Exception) {
        catchLog("getStatusBarHeight: ${gsonString(e)}")
        return 0
    }
}

fun Activity.getActionBarHeight(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
    return TypedValue.complexToDimensionPixelOffset(typedValue.data, resources.displayMetrics)
}