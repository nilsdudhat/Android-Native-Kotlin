package com.belive.dating.activities.edit_profile.opposite_gender

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.user.UserResponse
import com.belive.dating.constants.OPPOSITE_GENDER_OPTIONS
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling the user's opposite gender preference editing.
 *
 * This ViewModel interacts with the UserRepository to update the user's preferred
 * opposite gender and manages the UI state related to the editing process.  It uses
 * Data Binding with ObservableFields for UI updates and handles saving/restoring
 * state using SavedStateHandle.
 *
 * Key Functionality:
 * - **UI State Management:** Uses [ObservableField]s for [isButtonEnabled] (to control
 *   the save button's enabled state) and [selectedOppositeGender] (to hold the user's
 *   current selection).  These fields are automatically observed by the UI via Data Binding.
 *
 * - **State Persistence:** Leverages [SavedStateHandle] to persist the values of
 *   [isButtonEnabled] and [selectedOppositeGender] across configuration changes (e.g.,
 *   screen rotations) and process death. The state is saved and restored using keys
 *   prefixed with the [TAG].
 *
 * - **User Interaction:**  Provides [saveOppositeGender] to initiate the process of
 *   updating the user's opposite gender preference on the server.  This function
 *   returns a [MutableStateFlow] emitting [Resource] objects to represent the different
 *   states of the operation (loading, success, error, etc.).
 *
 * - **Data Access:** Uses Koin for dependency injection to obtain an instance of
 *   [UserRepository], responsible for interacting with the user data API.
 *
 * - **Asynchronous Operations:** Employs [viewModelScope] and [Dispatchers.IO] to
 *   perform network operations asynchronously, preventing UI thread blocking.
 *
 * - **Error Handling:** Handles different response codes from the API, including
 *   success, client errors (e.g., unauthorized, forbidden), and generic errors. It
 *   emits appropriate [Resource] states based on the response.
 */
class EditOppositeGenderViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_OPPOSITE_GENDER_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val selectedOppositeGender = ObservableField<OPPOSITE_GENDER_OPTIONS?>() // opposite gender id

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedOppositeGender"] = selectedOppositeGender.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedOppositeGender.set(savedStateHandle["${TAG}_selectedOppositeGender"])
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    fun saveOppositeGender(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("seeing_interest", selectedOppositeGender.get()?.genderIndex)

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--opposite_gender--", "request.url: ${response.raw().request.url}")
            logger("--opposite_gender--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--opposite_gender--", "code: ${response.code()}")
            logger("--opposite_gender--", "isSuccessful: ${response.isSuccessful}")
            logger("--opposite_gender--", "errorBody: $errorBody")
            logger("--opposite_gender--", "body: ${gsonString(response.body())}")
            logger("--opposite_gender--", "query: ${gsonString(json)}")

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