package com.belive.dating.extensions

import android.util.Log
import com.belive.dating.BuildConfig

fun logger(key: String, value: Any?) {
    if (BuildConfig.DEBUG) {
        if (value is String?) {
            Log.d(key, value ?: "")
        } else {
            Log.d(key, gsonString(value))
        }
    }
}

fun catchLog(value: Any?) {
    if (BuildConfig.DEBUG) {
        if (value is String?) {
            Log.e("--catch--", value ?: "")
        } else {
            Log.e("--catch--", gsonString(value))
        }
    }
}