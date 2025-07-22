package com.belive.dating.activities.filter.location

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.my_locations.MyLocation
import com.belive.dating.api.user.models.my_locations.MyLocationsResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ChangeLocationViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val currentLocation = ObservableField("-")
    val isLocationChanged = ObservableField(false)
    val isCurrentLocationSelected = ObservableField(true)
    val isDeleteView = ObservableField(false)
    val isDeleteEnabled = ObservableField(false)
    val isDeleteAvailable = ObservableField(false)
    val isDataAvailable = ObservableField(false)
    val customLocationList = ObservableField<ArrayList<MyLocation>>(arrayListOf())
    val selectedLocation = ObservableField<Triple<Double?, Double?, String?>>()
    val deletedList = ObservableField<ArrayList<MyLocation>>(arrayListOf())

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    private val userRepository = getKoinObject().get<UserRepository>()

    fun getMyLocation(): MutableStateFlow<Resource<MyLocationsResponse?>> {
        val data = MutableStateFlow<Resource<MyLocationsResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getMyLocations()
                val errorBody = response.errorBody()?.string()

                logger("--location--", "request.url: ${response.raw().request.url}")
                logger("--location--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--location--", "code: ${response.code()}")
                logger("--location--", "isSuccessful: ${response.isSuccessful}")
                logger("--location--", "errorBody: $errorBody")
                logger("--location--", "body: ${gsonString(response.body())}")
                logger("--location--", "query: ${gsonString(response.raw().request.url.query)}")

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

    fun deleteLocations(delete: String): MutableStateFlow<Resource<MyLocationsResponse?>> {
        val data = MutableStateFlow<Resource<MyLocationsResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.deleteLocations(delete)
                val errorBody = response.errorBody()?.string()

                logger("--delete_location--", "request.url: ${response.raw().request.url}")
                logger("--delete_location--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--delete_location--", "code: ${response.code()}")
                logger("--delete_location--", "isSuccessful: ${response.isSuccessful}")
                logger("--delete_location--", "errorBody: $errorBody")
                logger("--delete_location--", "body: ${gsonString(response.body())}")
                logger("--delete_location--", "query: ${gsonString(response.raw().request.url.query)}")

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

    fun updateFilters(mutableMap: MutableMap<String, Any?>): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        logger("--filters--", gsonString(mutableMap))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.updateFilters(mutableMap)
                val errorBody = response.errorBody()?.string()

                logger("--filter--", "request.url: ${response.raw().request.url}")
                logger("--filter--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--filter--", "code: ${response.code()}")
                logger("--filter--", "isSuccessful: ${response.isSuccessful}")
                logger("--filter--", "errorBody: $errorBody")
                logger("--filter--", "body: ${gsonString(response.body())}")
                logger("--filter--", "query: ${gsonString(response.raw().request.url.query)}")

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

}