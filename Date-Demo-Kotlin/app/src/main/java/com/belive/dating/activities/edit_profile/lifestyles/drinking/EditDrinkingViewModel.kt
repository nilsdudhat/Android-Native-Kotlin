package com.belive.dating.activities.edit_profile.lifestyles.drinking

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
 * ViewModel responsible for managing the state and logic related to editing a user's drinking habits.
 * It interacts with the [UserRepository] to update user details and persists state across configuration changes
 * using [SavedStateHandle].
 *
 * @property savedStateHandle Handle for saving and retrieving state across configuration changes.
 */
class EditDrinkingViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_DRINKING_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val selectedDrinking = ObservableField<String?>()

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedDrinking"] = selectedDrinking.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedDrinking.set(savedStateHandle["${TAG}_selectedDrinking"])
    }

    val drinkingList = arrayListOf<String>().apply {
        add("I don’t drink")
        add("Just quit drinking")
        add("Considering sobriety")
        add("Only drink on special occasions")
        add("Drink socially on weekends")
        add("Drink most nights")
        add("Open to drinking anytime")
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun saveDrinkingHabit(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("drinking", selectedDrinking.get() ?: "")

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_drinking--", "request.url: ${response.raw().request.url}")
            logger("--update_drinking--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_drinking--", "code: ${response.code()}")
            logger("--update_drinking--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_drinking--", "errorBody: $errorBody")
            logger("--update_drinking--", "body: ${gsonString(response.body())}")
            logger("--update_drinking--", "query: ${gsonString(json)}")

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