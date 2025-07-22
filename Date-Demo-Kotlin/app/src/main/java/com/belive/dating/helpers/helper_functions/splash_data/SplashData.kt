package com.belive.dating.helpers.helper_functions.splash_data

import androidx.lifecycle.lifecycleScope
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.belive.dating.BuildConfig
import com.belive.dating.api.country.CountryClient
import com.belive.dating.api.country.CountryService
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.extensions.getDeviceID
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.safeApiCallResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SplashData {

    val activity = getKoinActivity()

    // referrer data
    private var dataListType: String? = null

    // country data
    private var country: String? = null
    private var regionName: String? = null
    private var city: String? = null
    private var isp: String? = null
    private var org: String? = null

    fun referrers(
        onSuccess: () -> Unit,
        onError: (message: String?) -> Unit,
    ) {
        val mReferrerClient = InstallReferrerClient.newBuilder(activity).build()
        mReferrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> try {
                        val referrerDetails = mReferrerClient.installReferrer
                        val referrerUrl = referrerDetails.installReferrer

                        dataListType = if (referrerUrl.contains(getGistPrefs().gName)) {
                            getGistPrefs().gName
                        } else if (referrerUrl.contains(getGistPrefs().fName)) {
                            getGistPrefs().fName
                        } else {
                            "gd"
                        }

                        onSuccess.invoke()
                    } catch (e: Exception) {
                        onError.invoke(e.message)
                    }

                    else -> {
                        onError.invoke(responseCode.toString())
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                onError.invoke("Disconnected")
            }
        })
    }

    fun getCountryDataApi(
        onSuccess: () -> Unit,
        onError: (message: String?) -> Unit,
    ) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = CountryClient.getCountryInstance().create(CountryService::class.java)

                val countryData = safeApiCallResponse {
                    response.getCountryData(getGistPrefs().googleURL)
                }

                if (countryData.isSuccessful) {
                    val rootJson = countryData.body()

                    if (rootJson?.has("status") == true) {

                        if (rootJson.has("country")) {
                            country = rootJson.getAsJsonPrimitive("country").asString
                        }
                        if (rootJson.has("regionName")) {
                            regionName = rootJson.getAsJsonPrimitive("regionName").asString
                        }
                        if (rootJson.has("city")) {
                            city = rootJson.getAsJsonPrimitive("city").asString
                        }
                        if (rootJson.has("isp")) {
                            isp = rootJson.getAsJsonPrimitive("isp").asString
                        }
                        if (rootJson.has("org")) {
                            org = rootJson.getAsJsonPrimitive("org").asString
                        }

                        onSuccess.invoke()
                    } else {
                        onError.invoke("status false")
                    }
                } else {
                    onError.invoke(countryData.errorBody()?.string())
                }
            } catch (e: Exception) {
                onError.invoke("status false")
            }
        }
    }

    fun sendUserData(
        onSuccess: () -> Unit,
        onError: (message: String?) -> Unit,
    ) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val mutableMap = mutableMapOf<String, String?>()
                mutableMap["device_id"] = activity.getDeviceID()
                mutableMap["packagename"] = activity.packageName
                mutableMap["country"] = country
                mutableMap["state"] = regionName
                mutableMap["city"] = city
                mutableMap["isp"] = isp
                mutableMap["org"] = org
                mutableMap["splash_users_by"] = dataListType
                mutableMap["app_version"] = BuildConfig.VERSION_NAME

                val introductionService = IntroductionClient.getIntroductionInstance().create(IntroductionService::class.java)

                val splashResponse = introductionService.splashUser(mutableMap)
                val errorBody = splashResponse.errorBody()?.string()

                if (splashResponse.isSuccessful) {
                    onSuccess.invoke()
                } else {
                    if (!errorBody.isNullOrEmpty()) {
                        onError.invoke(getErrorMessage(errorBody))
                    } else {
                        onError.invoke("Something went wrong...!")
                    }
                }
            } catch (e: Exception) {
                onError.invoke(e.message)
            }
        }
    }
}