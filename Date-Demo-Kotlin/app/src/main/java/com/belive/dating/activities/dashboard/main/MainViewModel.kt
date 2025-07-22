package com.belive.dating.activities.dashboard.main

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.gist.GISTRepository
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.payment.PaymentResponse
import com.belive.dating.api.user.models.user.UserResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.safeApiCallResponse
import com.belive.dating.extensions.toJsonObject
import com.belive.dating.payment.ProductType
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class MainViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(), KoinComponent {

    val TAG = "MAIN_VIEW_MODEL"

    var isInitFailed = ObservableField(false)
    var isGPSEnabled = ObservableField(true)
    var isInitLoading = ObservableField(true)
    var isAppReviewed = ObservableField(false)
    var activePackage = ObservableField<ProductType>()
    var currentFragment = ObservableField("home")

    fun updateState() {
        savedStateHandle["${TAG}_isInitFailed"] = isInitFailed.get()
        savedStateHandle["${TAG}_isGPSEnabled"] = isGPSEnabled.get()
        savedStateHandle["${TAG}_isInitLoading"] = isInitLoading.get()
        savedStateHandle["${TAG}_activePackage"] = activePackage.get()
        savedStateHandle["${TAG}_currentFragment"] = currentFragment.get()
    }

    fun getState() {
        isInitFailed.set(savedStateHandle["${TAG}_isInitFailed"])
        isGPSEnabled.set(savedStateHandle["${TAG}_isGPSEnabled"])
        isInitLoading.set(savedStateHandle["${TAG}_isInitLoading"])
        activePackage.set(savedStateHandle["${TAG}_activePackage"])
        currentFragment.set(savedStateHandle["${TAG}_currentFragment"])
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun updateUserLocation(locationMap: Map<String, String>): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val initTime = System.currentTimeMillis()

                val response = userRepository.updateUserDetails(locationMap.toJsonObject())
                val errorBody = response.errorBody()?.string()

                logger("--update_location--", "loadingTime: ${(System.currentTimeMillis() - initTime)}")
                logger("--update_location--", "request.url: ${response.raw().request.url}")
                logger("--update_location--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--update_location--", "code: ${response.code()}")
                logger("--update_location--", "isSuccessful: ${response.isSuccessful}")
                logger("--update_location--", "errorBody: $errorBody")
                logger("--update_location--", "body: ${gsonString(response.body())}")
                logger("--update_location--", "request: ${gsonString(locationMap)}")

                if (response.isSuccessful) {
                    data.emit(Resource.success(response.body()))
                } else {
                    if (response.code() == 401) {
                        data.emit(Resource.signOut(getErrorMessage(errorBody), null))
                    } else if (response.code() == 403) {
                        data.emit(Resource.adminBlocked(getErrorMessage(errorBody), null))
                    } else {
                        if (!errorBody.isNullOrEmpty()) {
                            data.emit(Resource.error(getErrorMessage(errorBody), null))
                        } else {
                            data.emit(Resource.error("Something went wrong...!", null))
                        }
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }

    fun makePurchase(map: MutableMap<String, Any?>): MutableStateFlow<Resource<PaymentResponse?>> {
        val data = MutableStateFlow<Resource<PaymentResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.makePurchase(map)
                val errorBody = response.errorBody()?.string()

                logger("--make_purchase--", "request.url: ${response.raw().request.url}")
                logger("--make_purchase--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--make_purchase--", "code: ${response.code()}")
                logger("--make_purchase--", "isSuccessful: ${response.isSuccessful}")
                logger("--make_purchase--", "errorBody: $errorBody")
                logger("--make_purchase--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    data.emit(Resource.success(response.body()))
                } else {
                    if (response.code() == 401) {
                        data.emit(Resource.signOut(getErrorMessage(errorBody), null))
                    } else if (response.code() == 403) {
                        data.emit(Resource.adminBlocked(getErrorMessage(errorBody), null))
                    } else {
                        if (!errorBody.isNullOrEmpty()) {
                            data.emit(Resource.error(getErrorMessage(errorBody), null))
                        } else {
                            data.emit(Resource.error("Something went wrong...!", null))
                        }
                    }
                }
            } catch (e: Exception) {
                catchLog("makePurchase: ${gsonString(e)}")

                data.emit(Resource.error("Something went wrong...!", null))
            }
        }

        return data
    }

    fun cancelPurchase(): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.cancelPurchase()
                val errorBody = response.errorBody()?.string()

                logger("--cancel_purchase--", "request.url: ${response.raw().request.url}")
                logger("--cancel_purchase--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--cancel_purchase--", "code: ${response.code()}")
                logger("--cancel_purchase--", "isSuccessful: ${response.isSuccessful}")
                logger("--cancel_purchase--", "errorBody: $errorBody")
                logger("--cancel_purchase--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    data.emit(Resource.success(response.body()))
                } else {
                    if (response.code() == 401) {
                        data.emit(Resource.signOut(getErrorMessage(errorBody), null))
                    } else if (response.code() == 403) {
                        data.emit(Resource.adminBlocked(getErrorMessage(errorBody), null))
                    } else {
                        if (!errorBody.isNullOrEmpty()) {
                            data.emit(Resource.error(getErrorMessage(errorBody), null))
                        } else {
                            data.emit(Resource.error("Something went wrong...!", null))
                        }
                    }
                }
            } catch (e: Exception) {
                logger("--gist--", "body: ${gsonString(e)}")

                data.emit(Resource.error("Something went wrong...!", null))
            }
        }

        return data
    }

    fun getRaw(): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
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
                        data.emit(Resource.success(response.body()))
                    } else {
                        if (!errorBody.isNullOrEmpty()) {
                            data.emit(Resource.error(getErrorMessage(errorBody), null))
                        } else {
                            data.emit(Resource.error("Something went wrong...!", null))
                        }
                    }
                } else {
                    data.emit(Resource.error("Something went wrong...!", null))
                }
            } catch (e: Exception) {
                logger("--gist--", "body: ${gsonString(e)}")

                data.emit(Resource.error("Something went wrong...!", null))
            }
        }

        return data
    }
}