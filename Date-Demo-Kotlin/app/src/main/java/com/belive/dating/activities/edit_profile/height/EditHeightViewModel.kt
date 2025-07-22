package com.belive.dating.activities.edit_profile.height

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
 * ViewModel for the "Edit Height" screen.
 *
 * This ViewModel manages user input for height (feet and inches, or centimeters),
 * handles saving the height to the user's profile, and persists UI state across
 * configuration changes using [SavedStateHandle].
 *
 * Key Features:
 * - Supports height input in both feet/inches and centimeters.
 * - Uses [ObservableField] for data binding and UI updates.
 * - Persists input values and UI state (e.g., selected unit, button enabled state) using [SavedStateHandle] to survive configuration changes.
 * - Interacts with a [UserRepository] to save height information to the backend.
 * - Provides a [MutableStateFlow] to emit the result of the save operation, handling success, errors (including specific error types like 401 for sign-out and 403 for admin block), and loading states.
 * - Uses coroutines and [Dispatchers.IO] for background operations to avoid blocking the main thread.
 * - Includes logging for debugging API requests and responses.
 *
 *  **Data Binding:**
 *  - `feet`: User's height in feet (as a String).
 *  - `inch`: User's height in inches (as a String).
 *  - `centimeter`: User's height in centimeters (as a String).
 *  - `isHeightInCentimetre`: Boolean flag indicating if the height is currently being edited in centimeters.
 *  - `isButtonEnabled`: Boolean flag indicating if the save button is enabled.
 */
class EditHeightViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_HEIGHT_VIEW_MODEL"

    val feet = ObservableField<String?>()
    val inch = ObservableField<String?>()
    val centimeter = ObservableField<String?>()
    val isHeightInCentimetre = ObservableField(true)
    val isButtonEnabled = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_feet"] = feet.get()
        savedStateHandle["${TAG}_inch"] = inch.get()
        savedStateHandle["${TAG}_centimeter"] = centimeter.get()
        savedStateHandle["${TAG}_isHeightInCentimetre"] = isHeightInCentimetre.get()
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
    }

    fun getState() {
        feet.set(savedStateHandle["${TAG}_feet"])
        inch.set(savedStateHandle["${TAG}_inch"])
        centimeter.set(savedStateHandle["${TAG}_centimeter"])
        isHeightInCentimetre.set(savedStateHandle["${TAG}_isHeightInCentimetre"])
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

    fun saveHeight(feet: Int, inch: Int): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            val heightJson = JsonObject()
            heightJson.addProperty("feet", feet)
            heightJson.addProperty("inch", inch)
            json.add("height", heightJson)

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_height--", "request.url: ${response.raw().request.url}")
            logger("--update_height--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_height--", "code: ${response.code()}")
            logger("--update_height--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_height--", "errorBody: $errorBody")
            logger("--update_height--", "body: ${gsonString(response.body())}")
            logger("--update_height--", "query: ${gsonString(json)}")

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