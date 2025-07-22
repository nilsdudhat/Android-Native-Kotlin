package com.belive.dating.activities.edit_profile.lifestyles.diet

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
 * ViewModel for the Edit Diet screen.  Handles user interactions related to updating their preferred diet.
 *
 * @param savedStateHandle A handle to saved state, allowing the ViewModel to persist data across configuration changes.
 */
class EditDietViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_DIET_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val selectedDiet = ObservableField<String?>()

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedDiet"] = selectedDiet.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedDiet.set(savedStateHandle["${TAG}_selectedDiet"])
    }

    val dietList = arrayListOf<String>().apply {
        add("Vegetarian")
        add("Vegan")
        add("Omnivore")
        add("Kosher")
        add("Carnivore")
        add("Pescatarian")
        add("Halal")
        add("Other")
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun saveDiet(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("preffered_diet", selectedDiet.get() ?: "")

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_diet--", "request.url: ${response.raw().request.url}")
            logger("--update_diet--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_diet--", "code: ${response.code()}")
            logger("--update_diet--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_diet--", "errorBody: $errorBody")
            logger("--update_diet--", "body: ${gsonString(response.body())}")
            logger("--update_diet--", "query: ${gsonString(json)}")

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