package com.belive.dating.preferences.pref_utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.belive.dating.constants.AdsConstants

class AdPrefUtils(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(AdsConstants.PREF_NAME, MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun getPrefEditor(): SharedPreferences.Editor {
        return editor
    }

    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun removeKey(key: String) {
        editor.remove(key).apply()
    }

    fun clear() {
        editor.clear().apply()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun getInt(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    fun setInt(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun setString(key: String, value: String?) {
        editor.putString(key, value).apply()
    }

    fun getLong(key: String, default: Long): Long {
        return sharedPreferences.getLong(key, default)
    }

    fun setLong(key: String, value: Long) {
        editor.putLong(key, value).apply()
    }

    fun getDouble(key: String, default: Double): Double {
        return sharedPreferences.getString(key, default.toString())?.toDouble() ?: default
    }

    fun setDouble(key: String, value: Double) {
        editor.putString(key, value.toString()).apply()
    }

    fun getFloat(key: String, default: Float): Float {
        return sharedPreferences.getFloat(key, default)
    }

    fun setFloat(key: String, value: Float) {
        editor.putFloat(key, value).apply()
    }

    fun getStringList(key: String): List<String> {
        val set = sharedPreferences.getStringSet(key, setOf())
        return set?.toList() ?: listOf()
    }

    fun setStringList(key: String, value: List<String>?) {
        val set = value?.toSet()
        editor.putStringSet(key, set).apply()
    }
}