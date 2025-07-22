package com.belive.dating.activities.edit_profile.interests

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.introduction.models.InterestData
import com.belive.dating.api.introduction.models.InterestsResponse
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
 * ViewModel for the Edit Interests screen.  Handles loading all interests, managing selected interests,
 * saving the updated interests to the user's profile, and maintaining UI state across configuration changes.
 *
 * @property savedStateHandle A handle to saved state, used to persist UI state across configuration changes.
 */
class EditInterestsViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_INTERESTS_VIEW_MODEL"

    val interestsCount = ObservableField(0)
    val selectedInterests = ObservableField<ArrayList<Int>?>()
    val interestsList = ObservableField<ArrayList<InterestData>?>()
    val isButtonEnabled = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_interestsCount"] = interestsCount.get()
        savedStateHandle["${TAG}_selectedInterests"] = gsonString(selectedInterests.get())
        savedStateHandle["${TAG}_interestsList"] = gsonString(interestsList.get())
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
    }

    fun getState() {
        interestsCount.set(savedStateHandle["${TAG}_interestsCount"])
        selectedInterests.set(savedStateHandle.get<String>("${TAG}_selectedInterests")?.fromJson())
        interestsList.set(savedStateHandle.get<String>("${TAG}_interestsList")?.fromJson())
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

    fun getAllInterests(): MutableStateFlow<Resource<InterestsResponse?>> {
        val resource = MutableStateFlow<Resource<InterestsResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.getAllInterests()
            val errorBody = response.errorBody()?.string()

            logger("--interests--", "request.url: ${response.raw().request.url}")
            logger("--interests--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--interests--", "code: ${response.code()}")
            logger("--interests--", "isSuccessful: ${response.isSuccessful}")
            logger("--interests--", "errorBody: $errorBody")
            logger("--interests--", "body: ${gsonString(response.body())}")

            if (response.isSuccessful) {
                if (response.body() != null) {
                    resource.emit(Resource.success(response.body()))
                } else {
                    resource.emit(Resource.error("Something went wrong...!", null))
                }
            } else {
                if (!errorBody.isNullOrEmpty()) {
                    resource.emit(Resource.error(getErrorMessage(errorBody), null))
                } else {
                    resource.emit(Resource.error("Something went wrong...!", null))
                }
            }
        }
        return resource
    }

    fun saveInterests(myInterests: String): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("my_interests", myInterests)

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_interests--", "request.url: ${response.raw().request.url}")
            logger("--update_interests--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_interests--", "code: ${response.code()}")
            logger("--update_interests--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_interests--", "errorBody: $errorBody")
            logger("--update_interests--", "body: ${gsonString(response.body())}")
            logger("--update_interests--", "query: ${gsonString(json)}")

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