package com.belive.dating.activities.edit_profile.your_styles.love_type

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
 * ViewModel responsible for managing the state and logic for editing a user's love types.
 *
 * This ViewModel handles:
 *  - Tracking the selected love types in [selectedLoveTypeList].
 *  - Enabling/disabling the save button based on selection (controlled by [isButtonEnabled]).
 *  - Tracking the number of selected love types with [loveTypeCount].
 *  - Persisting and retrieving state across process deaths and configuration changes using [SavedStateHandle].
 *  - Saving the updated love types to the user's profile via the [UserRepository] through the [saveLoveTypes] function.
 *
 * The love types are a predefined list available in [loveTypeList].  User interactions (selection/deselection)
 * should update [selectedLoveTypeList], [loveTypeCount], and [isButtonEnabled] accordingly.  The [updateState]
 * and [getState] functions manage persistence using the provided [savedStateHandle].
 *
 *  @property savedStateHandle Provides access to the Saved State feature, allowing the ViewModel to
 *                             retain state across process deaths and configuration changes.
 *  @property isButtonEnabled An [ObservableField] indicating whether the save button should be enabled (true) or disabled (false).
 *  @property loveTypeCount An [ObservableField] containing the number of love types currently selected by the user.
 *  @property selectedLoveTypeList An [ObservableField] containing an [ArrayList] of [String] representing the love types selected by the user.
 */
class EditLoveTypeViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_LOVE_TYPE_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val loveTypeCount = ObservableField(0)
    val selectedLoveTypeList = ObservableField<ArrayList<String>>(arrayListOf())

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_loveTypeCount"] = loveTypeCount.get()
        savedStateHandle["${TAG}_selectedLoveTypeList"] = gsonString(selectedLoveTypeList.get())
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        loveTypeCount.set(savedStateHandle["${TAG}_loveTypeCount"])
        selectedLoveTypeList.set(savedStateHandle.get<String>("${TAG}_selectedLoveTypeList")?.fromJson())
    }

    val loveTypeList = arrayListOf<String>().apply {
        add("Gifts")
        add("Sweet words")
        add("Hugs & touch")
        add("Quality time")
        add("Kind actions")
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun saveLoveTypes(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("love_type", selectedLoveTypeList.get()?.joinToString(",") { it })

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_love_type--", "request.url: ${response.raw().request.url}")
            logger("--update_love_type--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_love_type--", "code: ${response.code()}")
            logger("--update_love_type--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_love_type--", "errorBody: $errorBody")
            logger("--update_love_type--", "body: ${gsonString(response.body())}")
            logger("--update_love_type--", "query: ${gsonString(json)}")

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