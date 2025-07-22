package com.belive.dating.activities.filter

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.filters.Filters
import com.belive.dating.api.user.models.filters.FiltersResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FiltersViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "FILTERS_VIEW_MODEL"

    val isFilterUpdated = ObservableField(false)
    val isDataLoaded = ObservableField(false)
    val ageMin = ObservableField(-1)
    val ageMax = ObservableField(-1)
    val distance = ObservableField(-1)
    val oppositeGender = ObservableField(-1)
    val location = ObservableField<Triple<Double?, Double?, String?>>()
    val filters = ObservableField<Filters>()
    val isDistanceInKms = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_isFilterUpdated"] = isFilterUpdated.get()
        savedStateHandle["${TAG}_isDataLoaded"] = isDataLoaded.get()
        savedStateHandle["${TAG}_ageMin"] = ageMin.get()
        savedStateHandle["${TAG}_ageMax"] = ageMax.get()
        savedStateHandle["${TAG}_distance"] = distance.get()
        savedStateHandle["${TAG}_oppositeGender"] = oppositeGender.get()
        savedStateHandle["${TAG}_location"] = location.get()
        savedStateHandle["${TAG}_filters"] = filters.get()
        savedStateHandle["${TAG}_isDistanceInKms"] = filters.get()
    }

    fun getState() {
        isFilterUpdated.set(savedStateHandle["${TAG}_isFilterUpdated"])
        isDataLoaded.set(savedStateHandle["${TAG}_isDataLoaded"])
        ageMin.set(savedStateHandle["${TAG}_ageMin"])
        ageMax.set(savedStateHandle["${TAG}_ageMax"])
        distance.set(savedStateHandle["${TAG}_distance"])
        oppositeGender.set(savedStateHandle["${TAG}_oppositeGender"])
        location.set(savedStateHandle["${TAG}_location"])
        filters.set(savedStateHandle["${TAG}_filters"])
        isDistanceInKms.set(savedStateHandle["${TAG}_isDistanceInKms"])
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    fun getFiltersData(): MutableStateFlow<Resource<FiltersResponse?>> {
        val data = MutableStateFlow<Resource<FiltersResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getFiltersData()
                val errorBody = response.errorBody()?.string()

                logger("--filter--", "request.url: ${response.raw().request.url}")
                logger("--filter--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--filter--", "code: ${response.code()}")
                logger("--filter--", "isSuccessful: ${response.isSuccessful}")
                logger("--filter--", "errorBody: $errorBody")
                logger("--filter--", "body: ${gsonString(response.body())}")

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
                catchLog("--filter-- ${gsonString(e)}")

                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }
}