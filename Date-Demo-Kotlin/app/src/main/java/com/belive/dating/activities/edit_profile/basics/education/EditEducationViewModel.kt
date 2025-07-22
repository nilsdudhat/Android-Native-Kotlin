package com.belive.dating.activities.edit_profile.basics.education

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.introduction.models.EducationData
import com.belive.dating.api.introduction.models.EducationResponse
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

/**
 * ViewModel responsible for managing the user's education information.
 *
 * This ViewModel interacts with the [UserRepository] to fetch and update education details.
 * It uses [SavedStateHandle] to persist UI state across configuration changes.
 *
 * @property savedStateHandle The [SavedStateHandle] used to save and restore UI state.
 */
class EditEducationViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_EDUCATION_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val educationList = ObservableField<ArrayList<EducationData>?>()
    val selectedEducation = ObservableField<Int?>() // education id

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedEducation"] = selectedEducation.get()
        savedStateHandle["${TAG}_educationList"] = gsonString(educationList.get())
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedEducation.set(savedStateHandle["${TAG}_selectedEducation"])
        educationList.set(savedStateHandle.get<String>("${TAG}_educationList")?.fromJson())
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getAllEducation(): MutableStateFlow<Resource<EducationResponse?>> {
        val resource = MutableStateFlow<Resource<EducationResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.getAllEducation()
            val errorBody = response.errorBody()?.string()

            logger("--educations--", "request.url: ${response.raw().request.url}")
            logger("--educations--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--educations--", "code: ${response.code()}")
            logger("--educations--", "isSuccessful: ${response.isSuccessful}")
            logger("--educations--", "errorBody: $errorBody")
            logger("--educations--", "body: ${gsonString(response.body())}")

            if (response.isSuccessful) {
                resource.emit(Resource.success(response.body()))
            } else {
                if (response.code() == 401) {
                    resource.emit(Resource.signOut(getErrorMessage(errorBody), null))
                } else if (response.code() == 403) {
                    resource.emit(Resource.adminBlocked(getErrorMessage(errorBody), null))
                } else {
                    if (!errorBody.isNullOrEmpty()) {
                        resource.emit(Resource.error(getErrorMessage(errorBody), null))
                    } else {
                        resource.emit(Resource.error("Something went wrong...!", null))
                    }
                }
            }
        }

        return resource
    }

    fun saveEducation(educationId: Int?): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val json = JsonObject()
            json.addProperty("education", educationId)

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_education--", "request.url: ${response.raw().request.url}")
            logger("--update_education--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_education--", "code: ${response.code()}")
            logger("--update_education--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_education--", "errorBody: $errorBody")
            logger("--update_education--", "body: ${gsonString(response.body())}")
            logger("--update_education--", "query: ${gsonString(json)}")

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