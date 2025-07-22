package com.belive.dating.activities.edit_profile.lifestyles.workout

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
 * ViewModel for the Edit Workout screen.
 * Manages the user's selected workout frequency and handles saving changes to the backend.
 *
 * @property savedStateHandle Used to persist UI state across configuration changes.  This allows the selected workout and button enabled state to be preserved across screen rotations or app process restarts.
 */
class EditWorkoutViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_WORKOUT_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val selectedWorkout = ObservableField<String?>()

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedWorkout"] = selectedWorkout.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedWorkout.set(savedStateHandle["${TAG}_selectedWorkout"])
    }

    val workoutList = arrayListOf<String>().apply {
        add("Everyday")
        add("Often")
        add("Sometimes")
        add("Never")
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun saveWorkoutHabit(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("workout", selectedWorkout.get() ?: "")

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_workout--", "request.url: ${response.raw().request.url}")
            logger("--update_workout--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_workout--", "code: ${response.code()}")
            logger("--update_workout--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_workout--", "errorBody: $errorBody")
            logger("--update_workout--", "body: ${gsonString(response.body())}")
            logger("--update_workout--", "query: ${gsonString(json)}")

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