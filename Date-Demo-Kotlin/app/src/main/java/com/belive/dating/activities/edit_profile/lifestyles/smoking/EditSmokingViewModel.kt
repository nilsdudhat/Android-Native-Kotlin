package com.belive.dating.activities.edit_profile.lifestyles.smoking

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

/**
 * ViewModel responsible for managing the editing of a user's smoking habits.
 *
 * This ViewModel handles user interactions for selecting and saving smoking preferences.
 * It uses [SavedStateHandle] to persist the selected smoking status across configuration changes.
 * It communicates with the [UserRepository] to update the user's smoking information on the server.
 *
 * @property savedStateHandle A handle to saved state associated with this ViewModel, used to persist UI state.
 */
class EditSmokingViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_SMOKING_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val selectedSmoking = ObservableField<String?>()

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedSmoking"] = selectedSmoking.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedSmoking.set(savedStateHandle["${TAG}_selectedSmoking"])
    }

    val smokingList = arrayListOf<String>().apply {
        add("Non-smoker")
        add("Smoke occasionally")
        add("Smoke socially")
        add("Only smoke when drinking")
        add("Regular smoker")
        add("Trying to quit smoking")
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun saveSmokingHabit(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("smoking", selectedSmoking.get() ?: "")

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_smoking--", "request.url: ${response.raw().request.url}")
            logger("--update_smoking--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_smoking--", "code: ${response.code()}")
            logger("--update_smoking--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_smoking--", "errorBody: $errorBody")
            logger("--update_smoking--", "body: ${gsonString(response.body())}")
            logger("--update_smoking--", "query: ${gsonString(json)}")

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