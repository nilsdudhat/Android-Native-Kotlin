package com.belive.dating.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.ContextThemeWrapper
import androidx.annotation.ColorInt
import androidx.core.view.WindowCompat

fun Context.isCurrentThemeSameAsProvided(providedThemeResId: Int): Boolean {
    // Define the attribute to compare (e.g., colorPrimary)
    val attrs = intArrayOf(android.R.attr.colorPrimary)

    // Get the current theme's attribute
    val currentThemeTypedArray = obtainStyledAttributes(attrs)
    val currentThemeColor = currentThemeTypedArray.getColor(0, 0)
    currentThemeTypedArray.recycle()

    // Create a ContextThemeWrapper with the provided theme
    val providedThemeContext = ContextThemeWrapper(this, providedThemeResId)

    // Get the provided theme's attribute
    val providedThemeTypedArray = providedThemeContext.obtainStyledAttributes(attrs)
    val providedThemeColor = providedThemeTypedArray.getColor(0, 0)
    providedThemeTypedArray.recycle()

    // Compare the attributes
    return currentThemeColor == providedThemeColor
}

fun Activity.setSystemBarColors(
    @ColorInt color: Int,
) {
    // Manage light/dark icons for status and navigation bars
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.isAppearanceLightStatusBars = isLightTheme()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        insetsController.isAppearanceLightNavigationBars = isLightTheme()
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            view.setBackgroundColor(color)
            insets
        }
    }
    // Set status bar color
    window.statusBarColor = color
    // Set navigation bar color
    window.navigationBarColor = color
}

// Extension function to check if the app is in dark theme
fun Context.isDarkTheme(): Boolean {
    // Get the current uiMode from the configuration
    val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return uiMode == Configuration.UI_MODE_NIGHT_YES
}

// Extension function to check if the app is in light theme
fun Context.isLightTheme(): Boolean {
    // Get the current uiMode from the configuration
    val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return uiMode == Configuration.UI_MODE_NIGHT_NO
}