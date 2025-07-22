package com.belive.dating.activities.edit_profile.basics.marital_status

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.introduction.models.MaritalStatusData
import com.belive.dating.api.introduction.models.MaritalStatusResponse
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.user.UserResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class EditMaritalStatusViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_MARITAL_STATUS_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val maritalStatusList = ObservableField<ArrayList<MaritalStatusData>?>()
    val selectedMaritalStatus = ObservableField<Int?>() // marital status id

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedMaritalStatus"] = selectedMaritalStatus.get()
        savedStateHandle["${TAG}_maritalStatusList"] = gsonString(maritalStatusList.get())
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedMaritalStatus.set(savedStateHandle["${TAG}_selectedMaritalStatus"])
        maritalStatusList.set(savedStateHandle.get<String>("${TAG}_maritalStatusList")?.fromJson())
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getAllMaritalStatus(): MutableStateFlow<Resource<MaritalStatusResponse?>> {
        val resource = MutableStateFlow<Resource<MaritalStatusResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.getAllMaritalStatus()
            val errorBody = response.errorBody()?.string()

            logger("--all_marital_status--", "request.url: ${response.raw().request.url}")
            logger("--all_marital_status--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--all_marital_status--", "code: ${response.code()}")
            logger("--all_marital_status--", "isSuccessful: ${response.isSuccessful}")
            logger("--all_marital_status--", "errorBody: $errorBody")
            logger("--all_marital_status--", "body: ${gsonString(response.body())}")

            if (response.isSuccessful) {
                if (response.body() != null) {
                    resource.emit(Resource.success(response.body()))
                } else {
                    resource.emit(Resource.error("Something went wrong...!", null))
                }
            } else {
                if (!errorBody.isNullOrEmpty()) {
                    resource.emit(Resource.error(getErrorMessage(errorBody), null))
                } else {
                    resource.emit(Resource.error("Something went wrong...!", null))
                }
            }
        }

        return resource
    }

    fun saveMaritalStatus(maritalStatusId: Int?): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val json = JsonObject()
            json.addProperty("marital_status", maritalStatusId)

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_marital_status--", "request.url: ${response.raw().request.url}")
            logger("--update_marital_status--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_marital_status--", "code: ${response.code()}")
            logger("--update_marital_status--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_marital_status--", "errorBody: $errorBody")
            logger("--update_marital_status--", "body: ${gsonString(response.body())}")
            logger("--update_marital_status--", "query: ${gsonString(json)}")

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
        }

        return data
    }
}