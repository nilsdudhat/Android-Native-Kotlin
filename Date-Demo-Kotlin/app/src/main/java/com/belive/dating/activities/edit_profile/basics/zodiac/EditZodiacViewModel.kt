package com.belive.dating.activities.edit_profile.basics.zodiac

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.introduction.models.ZodiacData
import com.belive.dating.api.introduction.models.ZodiacResponse
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.user.UserResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling zodiac editing logic.
 *
 * This ViewModel is responsible for managing the UI state related to editing a user's zodiac sign.
 * It interacts with the [UserRepository] to fetch zodiac options and update the user's selected zodiac.
 * The state is persisted across configuration changes using [SavedStateHandle].
 *
 * @param savedStateHandle Handle for saving and retrieving UI state across configuration changes.
 */
class EditZodiacViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_ZODIAC_VIEW_MODEL"

    val isButtonEnabled = ObservableField(false)
    val zodiacList = ObservableField<ArrayList<ZodiacData>?>()
    val selectedZodiac = ObservableField<Int?>() // zodiac id

    fun updateState() {
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
        savedStateHandle["${TAG}_selectedZodiac"] = selectedZodiac.get()
        savedStateHandle["${TAG}_zodiacList"] = gsonString(zodiacList.get())
    }

    fun getState() {
        isButtonEnabled.set(savedStateHandle["${TAG}_isButtonEnabled"])
        selectedZodiac.set(savedStateHandle["${TAG}_selectedZodiac"])
        zodiacList.set(savedStateHandle.get<String>("${TAG}_zodiacList")?.fromJson())
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getAllZodiac(): MutableStateFlow<Resource<ZodiacResponse?>> {
        val resource = MutableStateFlow<Resource<ZodiacResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.getAllZodiac()
            val errorBody = response.errorBody()?.string()

            logger("--zodiacs--", "request.url: ${response.raw().request.url}")
            logger("--zodiacs--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--zodiacs--", "code: ${response.code()}")
            logger("--zodiacs--", "isSuccessful: ${response.isSuccessful}")
            logger("--zodiacs--", "errorBody: $errorBody")
            logger("--zodiacs--", "body: ${gsonString(response.body())}")

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

    fun saveZodiac(zodiacId: Int?): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val json = JsonObject()
            if (zodiacId == null) {
                json.add("zodiac", JsonNull.INSTANCE)
            } else {
                json.addProperty("zodiac", zodiacId)
            }

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_zodiac--", "request.url: ${response.raw().request.url}")
            logger("--update_zodiac--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_zodiac--", "code: ${response.code()}")
            logger("--update_zodiac--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_zodiac--", "errorBody: $errorBody")
            logger("--update_zodiac--", "body: ${gsonString(response.body())}")
            logger("--update_zodiac--", "query: ${gsonString(json)}")

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