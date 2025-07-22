package com.belive.dating.helpers.helper_functions.get_gist

import com.belive.dating.ads.ManageAds
import com.belive.dating.api.ads_settings.AdsSettingsRepository
import com.belive.dating.api.gist.GISTRepository
import com.belive.dating.di.adsSettingsModule
import com.belive.dating.extensions.getAdsPrefs
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.safeApiCallResponse
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.unloadKoinModules

fun getGistData(
    scope: CoroutineScope,
    isLoading: (Boolean) -> Unit,
    gistNotAvailable: () -> Unit,
    onError: (String) -> Unit,
    onSuccess: (JsonObject) -> Unit,
) {
    val isGistAvailable = try {
        getKoinObject().get<GISTRepository>()
        true
    } catch (e: Exception) {
        false
    }
    if (isGistAvailable) {
        isLoading.invoke(true)

        scope.launch(Dispatchers.IO) {
            try {
                val repository = getKoinObject().get<GISTRepository>() // Retrieve the repository
                val response = safeApiCallResponse {
                    repository.getRaw()
                }
                val errorBody = response.errorBody()?.string()

                logger("--gist--", "request.url: ${response.raw().request.url}")
                logger("--gist--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--gist--", "code: ${response.code()}")
                logger("--gist--", "isSuccessful: ${response.isSuccessful}")
                logger("--gist--", "errorBody: $errorBody")
                logger("--gist--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    if (response.body() != null) {
                        getGistPrefs().setGistData(response.body()!!)

                        val isAdsModuleAvailable = try {
                            getKoinObject().get<AdsSettingsRepository>()
                            true
                        } catch (e: Exception) {
                            false
                        }
                        if (isAdsModuleAvailable) {
                            val adsSettingsRepository = getKoinObject().get<AdsSettingsRepository>()

                            val adsResponse = safeApiCallResponse {
                                adsSettingsRepository.getAdsSettings()
                            }
                            val adsErrorBody = adsResponse.errorBody()?.string()

                            logger("--ads_settings--", "request.url: ${adsResponse.raw().request.url}")
                            logger("--ads_settings--", "request.body: ${gsonString(adsResponse.raw().request.body)}")
                            logger("--ads_settings--", "code: ${adsResponse.code()}")
                            logger("--ads_settings--", "isSuccessful: ${adsResponse.isSuccessful}")
                            logger("--ads_settings--", "errorBody: $adsErrorBody")
                            logger("--ads_settings--", "body: ${gsonString(adsResponse.body())}")

                            if (adsResponse.isSuccessful) {
                                if (adsResponse.body() != null) {
                                    getAdsPrefs().setAdsSettings(adsResponse.body()!!)

                                    unloadKoinModules(adsSettingsModule)
                                }
                            }

                            ManageAds.startLoadingAds = true

                            scope.launch(Dispatchers.Main) {
                                isLoading.invoke(false)
                                onSuccess.invoke(response.body()!!)
                            }
                        } else {
                            scope.launch(Dispatchers.Main) {
                                isLoading.invoke(false)
                                onSuccess.invoke(response.body()!!)
                            }
                        }
                    } else {
                        scope.launch(Dispatchers.Main) {
                            onError.invoke("Something went wrong...!")
                        }
                    }
                } else {
                    scope.launch(Dispatchers.Main) {
                        isLoading.invoke(false)
                        if (!errorBody.isNullOrEmpty()) {
                            onError.invoke(getErrorMessage(errorBody))
                        } else {
                            onError.invoke("Something went wrong...!")
                        }
                    }
                }
            } catch (e: Exception) {
                logger("--gist--", "catch: ${gsonString(e)}")

                scope.launch(Dispatchers.Main) {
                    isLoading.invoke(false)
                    onError.invoke(e.message ?: "Something went wrong...!")
                }
            }
        }
    } else {
        gistNotAvailable.invoke()
    }
}