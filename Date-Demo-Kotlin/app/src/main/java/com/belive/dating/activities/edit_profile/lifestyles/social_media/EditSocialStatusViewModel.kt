package com.belive.dating.activities.edit_profile.lifestyles.social_media

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
 * ViewModel for editing the user's social media status.
 *
 * This ViewModel manages the user interface state for editing a user's social media status.
 * It provides functionality for selecting a status from a predefined list, saving the
 * updated status to the backend, and persisting the UI state across configuration changes
 * using [SavedStateHandle].
 *
 * @property savedStateHandle A handle to saved state associated with this ViewModel.
 *                             Used to persist UI state across configuration changes.
 */
class EditSocialStatusViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_SOCIAL_STATUS_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val selectedStatus = ObservableField<String?>()

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedStatus"] = selectedStatus.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedStatus.set(savedStateHandle["${TAG}_selectedStatus"])
    }

    val statusList = arrayListOf<String>().apply {
        add("Content Creator")
        add("Checking Occasionally")
        add("Always Active")
        add("Passive Scroller")
        add("Not on Social Media")
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun saveStatus(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("social_media_status", selectedStatus.get() ?: "")

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_status--", "request.url: ${response.raw().request.url}")
            logger("--update_status--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_status--", "code: ${response.code()}")
            logger("--update_status--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_status--", "errorBody: $errorBody")
            logger("--update_status--", "body: ${gsonString(response.body())}")
            logger("--update_status--", "query: ${gsonString(json)}")

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