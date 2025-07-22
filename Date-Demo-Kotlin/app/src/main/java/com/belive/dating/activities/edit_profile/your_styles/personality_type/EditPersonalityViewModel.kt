package com.belive.dating.activities.edit_profile.your_styles.personality_type

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
 * ViewModel for the Edit Personality screen. Handles loading, saving, and managing the user's personality type selection.
 *
 * @property savedStateHandle A handle to saved state, allowing the view model to persist data across configuration changes and process death.
 * Specifically, it stores the state of [isButtonEnabled] and [selectedPersonalityList].
 */
class EditPersonalityViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_PERSONALITY_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val personalityCount = ObservableField(0)
    val selectedPersonalityList = ObservableField<ArrayList<String>>(arrayListOf())

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_personalityCount"] = personalityCount.get()
        savedStateHandle["${TAG}_selectedPersonalityList"] = gsonString(selectedPersonalityList.get())
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        personalityCount.set(savedStateHandle["${TAG}_personalityCount"])
        selectedPersonalityList.set(savedStateHandle.get<String>("${TAG}_selectedPersonalityList")?.fromJson())
    }

    val personalityList = arrayListOf<String>().apply {
        add("ENFJ")
        add("ESTP")
        add("ISTP")
        add("INFP")
        add("ISFP")
        add("ENTJ")
        add("INTJ")
        add("ESTJ")
        add("INTP")
        add("ENTP")
        add("ISFJ")
        add("ESFP")
        add("INFJ")
        add("ISTJ")
        add("ESFJ")
        add("ENFP")
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun savePersonalityTypes(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("personality_type", selectedPersonalityList.get()?.joinToString(",") { it })

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_personality_type--", "request.url: ${response.raw().request.url}")
            logger("--update_personality_type--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_personality_type--", "code: ${response.code()}")
            logger("--update_personality_type--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_personality_type--", "errorBody: $errorBody")
            logger("--update_personality_type--", "body: ${gsonString(response.body())}")
            logger("--update_personality_type--", "query: ${gsonString(json)}")

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