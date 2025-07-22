package com.belive.dating.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.belive.dating.extensions.getDeviceID
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.mixpanel.android.mpmetrics.MixpanelAPI

open class MixPanelActivity : EventBusActivity() {

    var mixPanel: MixpanelAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Declare the current activity as the singleton instance
        getKoinObject().declare(this as AppCompatActivity, allowOverride = true)
        super.onCreate(savedInstanceState)
        /*window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )*/

        if (getGistPrefs().mixpanelKey.isNotEmpty()) {
            mixPanel = MixpanelAPI.getInstance(this, getGistPrefs().mixpanelKey, true)
            mixPanel?.identify(getDeviceID())
            mixPanel?.setUseIpAddressForGeolocation(true)
            mixPanel?.people?.apply {
                if (getUserPrefs().userId != -1) {
                    set("User ID", getUserPrefs().userId)
                }

                val city = getUserPrefs().currentCity
                val state = getUserPrefs().currentState
                val country = getUserPrefs().currentCountry

                val location = if ((city == null) && (state == null) && (country == null)) {
                    null
                } else {
                    if (state == null) {
                        country
                    } else if (city == null) {
                        "$state, $country"
                    } else {
                        "$city, $state, $country"
                    }
                }
                if (location != null) {
                    set("Location", location)
                }

                val gender = getUserPrefs().gender
                if (gender != null) {
                    set("Gender", gender)
                }

                val birthDate = getUserPrefs().birthDate
                if (birthDate != null) {
                    set("Birth Date", birthDate)
                }

                val email = getUserPrefs().emailId
                if (email != null) {
                    set("Email", email)
                }

                val currentPackage = getUserPrefs().activePackage
                set("Package", currentPackage)
            }
        }

        logger("--token--", "Device ID: ${getDeviceID()}")
        logger("--token--", "Mixpanel ID: ${mixPanel?.distinctId}")
    }

    override fun onResume() {
        getKoinObject().declare(this as AppCompatActivity, allowOverride = true)
        super.onResume()
    }

    override fun onDestroy() {
        mixPanel?.flush()
        super.onDestroy()
    }
}