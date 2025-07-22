package com.belive.dating.onesignal

import android.content.Context
import com.belive.dating.extensions.getGistPrefs
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import java.util.Locale

object ManualOneSignal {

    private fun initialise(context: Context): Boolean {
        if (!OneSignal.isInitialized) {
            OneSignal.consentGiven = true

            // Verbose Logging set to help debug issues, remove before releasing your app.
            OneSignal.Debug.logLevel = LogLevel.VERBOSE

            if (getGistPrefs().appOneSingleID.isNotEmpty()) {
                OneSignal.initWithContext(
                    context,
                    getGistPrefs().appOneSingleID
                ) // OneSignal Initialization
                return true
            } else {
                return false
            }
        } else {
            return true
        }
    }

    fun setLanguage(context: Context) { // Setting User Device Language
        if (initialise(context)) {
            OneSignal.User.setLanguage(Locale.getDefault().language) // "en"
        }
    }

    fun consentRequired() { // Declare User Consent Required or not with OneSignal
        OneSignal.consentRequired = true
    }

    fun setEmail(context: Context, email: String) { // Setting User Email
        if (initialise(context)) {
            OneSignal.User.addEmail(email)
        }
    }

    fun shareLocation(context: Context) { // Share Location with OneSignal
        if (initialise(context)) {
            OneSignal.Location.isShared = true
        }
    }

    fun login(context: Context, userId: String) { // Login OneSignal with given userId
        if (initialise(context) && OneSignal.User.externalId.isEmpty()) {
            OneSignal.login(userId)
        }
    }

    fun logout() { // Logout OneSignal if OneSignal Initialised & user logged-in
        if (OneSignal.isInitialized && OneSignal.User.externalId.isNotEmpty()) {
            OneSignal.logout()
        }
    }
}