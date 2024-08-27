package com.sharedpreferences.app

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedPrefUtils(context: Context, encryptedPrefName: String) {

    private var masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private var sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        encryptedPrefName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun removeKey(key: String) {
        editor.remove(key)
        editor.apply()
    }

    fun clear() {
        editor.clear()
        editor.apply()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getInt(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    fun setInt(key: String, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    fun getString(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun setString(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun getLong(key: String, default: Long): Long {
        return sharedPreferences.getLong(key, default)
    }

    fun setLong(key: String, value: Long) {
        editor.putLong(key, value)
        editor.apply()
    }

    fun getDouble(key: String, default: Double): Double {
        return sharedPreferences.getString(key, default.toString())?.toDouble() ?: default
    }

    fun setDouble(key: String, value: Double) {
        editor.putString(key, value.toString())
        editor.apply()
    }

    fun getFloat(key: String, default: Float): Float {
        return sharedPreferences.getFloat(key, default)
    }

    fun setFloat(key: String, value: Float) {
        editor.putFloat(key, value)
        editor.apply()
    }

    fun getStringList(key: String): List<String> {
        val set = sharedPreferences.getStringSet(key, setOf())
        return set?.toList() ?: listOf()
    }

    fun setStringList(key: String, value: List<String>) {
        val set = value.toSet()
        editor.putStringSet(key, set)
        editor.apply()
    }
}