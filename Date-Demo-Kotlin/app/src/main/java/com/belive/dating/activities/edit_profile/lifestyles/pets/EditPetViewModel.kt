package com.belive.dating.activities.edit_profile.lifestyles.pets

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
 * ViewModel for editing the user's pet preference.
 *
 * This ViewModel manages the state of the edit pet screen, including the selected pet,
 * button enabled state, and interactions with the UserRepository to save the updated pet preference.
 * It also utilizes SavedStateHandle to persist the state across configuration changes.
 *
 * @property savedStateHandle The SavedStateHandle instance used to persist the ViewModel's state.
 */
class EditPetViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_PET_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val selectedPet = ObservableField<String?>()

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedPet"] = selectedPet.get()
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedPet.set(savedStateHandle["${TAG}_selectedPet"])
    }

    val petList = arrayListOf<String>().apply {
        add("Rabbit")
        add("Fish")
        add("Dog")
        add("Turtle")
        add("Amphibian")
        add("Cat")
        add("Hamster")
        add("Bird")
        add("Reptile")
        add("Other")
        add("Pet-free")
        add("Want a pet")
        add("Allergic to pets")
        add("Multiple pets")
        add("Don't have, but love")
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun savePets(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("pets", selectedPet.get() ?: "")

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_pet--", "request.url: ${response.raw().request.url}")
            logger("--update_pet--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_pet--", "code: ${response.code()}")
            logger("--update_pet--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_pet--", "errorBody: $errorBody")
            logger("--update_pet--", "body: ${gsonString(response.body())}")
            logger("--update_pet--", "query: ${gsonString(json)}")

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