package com.belive.dating.preferences.pref_utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.belive.dating.constants.EncryptedPrefConst
import java.security.KeyStore

class GistPrefUtils(context: Context) {

    /*private var masterKey: MasterKey = MasterKey.Builder(context, EncryptedPrefConst.PREF_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private var sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        EncryptedPrefConst.PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private var editor: SharedPreferences.Editor = sharedPreferences.edit()*/

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(EncryptedPrefConst.PREF_NAME, MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun getEncryptedPrefEditor(): SharedPreferences.Editor {
        return editor
    }

    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun removeStorageKey() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        if (keyStore.containsAlias(EncryptedPrefConst.PREF_ALIAS)) {
            keyStore.deleteEntry(EncryptedPrefConst.PREF_ALIAS)
        }
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

    fun getString(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun setString(key: String, value: String) {
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

    fun setStringList(key: String, value: List<String>) {
        val set = value.toSet()
        editor.putStringSet(key, set).apply()
    }
}