package com.belive.dating.activities.edit_profile.your_styles.communication_type

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * ViewModel for managing the editing of user communication preferences.
 *
 * This ViewModel handles the logic for displaying and updating the user's preferred
 * communication methods.  It interacts with the [UserRepository] to persist changes
 * and utilizes [SavedStateHandle] to preserve UI state across configuration changes
 * or process death.
 *
 * @property savedStateHandle A handle to saved state, allowing the ViewModel to persist
 *                           and restore UI state.
 */
class EditCommunicationViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_COMMUNICATION_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val communicationCount = ObservableField(0)
    val selectedCommunicationList = ObservableField<ArrayList<String>>(arrayListOf())

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_communicationCount"] = communicationCount.get()
        savedStateHandle["${TAG}_selectedCommunicationList"] = gsonString(selectedCommunicationList.get())
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        communicationCount.set(savedStateHandle["${TAG}_communicationCount"])
        selectedCommunicationList.set(savedStateHandle.get<String>("${TAG}_selectedCommunicationList")?.fromJson())
    }

    val communicationList = arrayListOf<String>().apply {
        add("Slow replier")
        add("Prefer calls")
        add("Always on WhatsApp")
        add("Better in person")
        add("Love video chats")
        add("Bad at texting")
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun saveCommunicationTypes(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("communication_type", selectedCommunicationList.get()?.joinToString(",") { it })

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_communication_type--", "request.url: ${response.raw().request.url}")
            logger("--update_communication_type--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_communication_type--", "code: ${response.code()}")
            logger("--update_communication_type--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_communication_type--", "errorBody: $errorBody")
            logger("--update_communication_type--", "body: ${gsonString(response.body())}")
            logger("--update_communication_type--", "query: ${gsonString(json)}")

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