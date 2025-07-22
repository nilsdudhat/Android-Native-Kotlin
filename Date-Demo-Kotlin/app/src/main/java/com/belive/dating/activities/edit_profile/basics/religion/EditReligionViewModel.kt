package com.belive.dating.activities.edit_profile.basics.religion

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.introduction.models.ReligionData
import com.belive.dating.api.introduction.models.ReligionsResponse
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

class EditReligionViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_RELIGION_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val religionList = ObservableField<ArrayList<ReligionData>?>()
    val selectedReligion = ObservableField<Int?>() // religion id

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_religionList"] = gsonString(religionList.get())
        savedStateHandle["${TAG}_selectedReligion"] = selectedReligion.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        religionList.set(savedStateHandle.get<String>("${TAG}_religionList")?.fromJson())
        selectedReligion.set(savedStateHandle["${TAG}_selectedReligion"])
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getAllReligions(): MutableStateFlow<Resource<ReligionsResponse?>> {
        val resource = MutableStateFlow<Resource<ReligionsResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.getAllReligions()
            val errorBody = response.errorBody()?.string()

            logger("--all_religions--", "request.url: ${response.raw().request.url}")
            logger("--all_religions--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--all_religions--", "code: ${response.code()}")
            logger("--all_religions--", "isSuccessful: ${response.isSuccessful}")
            logger("--all_religions--", "errorBody: $errorBody")
            logger("--all_religions--", "body: ${gsonString(response.body())}")

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

    fun saveReligion(religionId: Int?): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val json = JsonObject()
            json.addProperty("religion", religionId)

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_religion--", "request.url: ${response.raw().request.url}")
            logger("--update_religion--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_religion--", "code: ${response.code()}")
            logger("--update_religion--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_religion--", "errorBody: $errorBody")
            logger("--update_religion--", "body: ${gsonString(response.body())}")
            logger("--update_religion--", "query: ${gsonString(json)}")

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