package com.belive.dating.activities.edit_profile.sexual_orientation

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.introduction.models.Orientation
import com.belive.dating.api.introduction.models.SexualOrientationResponse
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
 * ViewModel for managing the user's sexual orientation information during editing.
 *
 * This ViewModel handles fetching the list of available sexual orientations,
 * saving the user's selection, and managing the state of the UI elements
 * related to the sexual orientation editing process.  It utilizes SavedStateHandle
 * to persist UI state across configuration changes.
 *
 * @property savedStateHandle  Provides access to saved state data, allowing the ViewModel to
 *                            restore data after process death or configuration changes.
 */
class EditSexualOrientationViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_SEXUAL_ORIENTATION_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val sexualOrientationList = ObservableField<ArrayList<Orientation>?>()
    val selectedSexualOrientation = ObservableField<Int?>()

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_sexualOrientationList"] = gsonString(sexualOrientationList.get())
        savedStateHandle["${TAG}_selectedSexualOrientation"] = selectedSexualOrientation.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        sexualOrientationList.set(savedStateHandle.get<String>("${TAG}_sexualOrientationList")?.fromJson())
        selectedSexualOrientation.set(savedStateHandle["${TAG}_selectedSexualOrientation"])
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getAllSexualOrientations(): MutableStateFlow<Resource<SexualOrientationResponse?>> {
        val resource = MutableStateFlow<Resource<SexualOrientationResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.getAllSexualOrientations()
            val errorBody = response.errorBody()?.string()

            logger("--sexual_orientations--", "request.url: ${response.raw().request.url}")
            logger("--sexual_orientations--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--sexual_orientations--", "code: ${response.code()}")
            logger("--sexual_orientations--", "isSuccessful: ${response.isSuccessful}")
            logger("--sexual_orientations--", "errorBody: $errorBody")
            logger("--sexual_orientations--", "body: ${gsonString(response.body())}")

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

    fun saveSexualOrientation(orientationId: Int): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("orientation_id", orientationId)

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_sexual_orientation--", "request.url: ${response.raw().request.url}")
            logger("--update_sexual_orientation--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_sexual_orientation--", "code: ${response.code()}")
            logger("--update_sexual_orientation--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_sexual_orientation--", "errorBody: $errorBody")
            logger("--update_sexual_orientation--", "body: ${gsonString(response.body())}")
            logger("--update_sexual_orientation--", "query: ${gsonString(json)}")

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
                        data.emit(Resource.error("Something went wrong", null))
                    }
                }
            }
        }

        return data
    }
}