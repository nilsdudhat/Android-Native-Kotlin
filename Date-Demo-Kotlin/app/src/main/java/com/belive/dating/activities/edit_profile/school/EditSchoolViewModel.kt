package com.belive.dating.activities.edit_profile.school

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.user.UserResponse
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

class EditSchoolMeViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_SCHOOL_VIEW_MODEL"

    val school = ObservableField<String?>()
    val isButtonEnabled = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_school"] = school.get()
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
    }

    fun getState() {
        school.set(savedStateHandle["${TAG}_school"])
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun saveSchool(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("school", school.get())

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_school--", "request.url: ${response.raw().request.url}")
            logger("--update_school--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_school--", "code: ${response.code()}")
            logger("--update_school--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_school--", "errorBody: $errorBody")
            logger("--update_school--", "body: ${gsonString(response.body())}")
            logger("--update_school--", "query: ${gsonString(json)}")

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

    fun validateName(): MutableStateFlow<Resource<JsonObject?>> {
        val resource = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.validateText(school.get()?.trim())
            val errorBody = response.errorBody()?.string()

            logger("--validate_name--", "request.url: ${response.raw().request.url}")
            logger("--validate_name--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--validate_name--", "code: ${response.code()}")
            logger("--validate_name--", "isSuccessful: ${response.isSuccessful}")
            logger("--validate_name--", "errorBody: $errorBody")
            logger("--validate_name--", "body: ${gsonString(response.body())}")

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
}