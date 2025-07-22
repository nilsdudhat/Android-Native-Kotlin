package com.belive.dating.helpers.helper_functions.tus_upload

import android.content.SharedPreferences
import io.tus.java.client.TusURLStore
import java.net.MalformedURLException
import java.net.URL
import androidx.core.content.edit

class TusPreferencesURLStore(private val preferences: SharedPreferences) : TusURLStore {

    override fun get(fingerprint: String): URL? {
        // Ignore empty fingerprints

        if (fingerprint.isEmpty()) {
            return null
        }

        val urlStr: String? = preferences.getString(fingerprint, "")

        // No entry was found
        if (urlStr.isNullOrEmpty()) {
            return null
        }

        // Ignore invalid URLs
        return try {
            URL(urlStr)
        } catch (e: MalformedURLException) {
            remove(fingerprint)
            null
        }
    }

    override fun set(fingerprint: String, url: URL) {
        val urlStr: String = url.toString()

        // Ignore empty fingerprints
        if (fingerprint.isEmpty()) {
            return
        }

        preferences.edit {
            putString(fingerprint, urlStr)
        }
    }

    override fun remove(fingerprint: String) {

        // Ignore empty fingerprints
        if (fingerprint.isEmpty()) {
            return
        }
        preferences.edit {
            remove(fingerprint)
        }
    }
}