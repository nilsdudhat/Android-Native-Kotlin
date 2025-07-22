package com.belive.dating.activities.edit_profile.languages

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionService
import com.belive.dating.api.introduction.models.LanguageData
import com.belive.dating.api.introduction.models.LanguagesResponse
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
 * ViewModel for the "Edit Languages" screen.
 *
 * This ViewModel manages the state and interactions for editing a user's known languages.
 * It retrieves the available languages, allows the user to select languages, and saves
 * the selected languages to the user's profile.  It also handles persisting and restoring
 * state across configuration changes using [SavedStateHandle].
 *
 * @property savedStateHandle  A handle to saved state, used for persisting and restoring data.
 */
class EditLanguagesViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "EDIT_LANGUAGES_VIEW_MODEL"

    val languagesCount = ObservableField(0)
    val selectedLanguages = ObservableField<ArrayList<String>?>()
    val languagesList = ObservableField<ArrayList<LanguageData>?>()
    val isButtonEnabled = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_languagesCount"] = languagesCount.get()
        savedStateHandle["${TAG}_selectedLanguages"] = selectedLanguages.get()
        savedStateHandle["${TAG}_languagesList"] = gsonString(languagesList.get())
        savedStateHandle["${TAG}_isButtonEnabled"] = isButtonEnabled.get()
    }

    fun getState() {
        languagesCount.set(savedStateHandle["${TAG}_languagesCount"])
        selectedLanguages.set(savedStateHandle["${TAG}_selectedLanguages"])
        languagesList.set(savedStateHandle.get<String>("${TAG}_languagesList")?.fromJson())
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

    fun getAllLanguages(): MutableStateFlow<Resource<LanguagesResponse?>> {
        val resource = MutableStateFlow<Resource<LanguagesResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            val introductionClient = IntroductionClient.getIntroductionInstance()
            val introductionService = introductionClient.create(IntroductionService::class.java)

            val response = introductionService.getAllLanguages()
            val errorBody = response.errorBody()?.string()

            logger("--all_languages--", "request.url: ${response.raw().request.url}")
            logger("--all_languages--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--all_languages--", "code: ${response.code()}")
            logger("--all_languages--", "isSuccessful: ${response.isSuccessful}")
            logger("--all_languages--", "errorBody: $errorBody")
            logger("--all_languages--", "body: ${gsonString(response.body())}")

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

    fun saveKnownLanguages(selectedLanguages: ArrayList<String>): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("known_language", selectedLanguages.joinToString(",") { it })

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_known_languages--", "request.url: ${response.raw().request.url}")
            logger("--update_known_languages--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_known_languages--", "code: ${response.code()}")
            logger("--update_known_languages--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_known_languages--", "errorBody: $errorBody")
            logger("--update_known_languages--", "body: ${gsonString(response.body())}")
            logger("--update_known_languages--", "query: ${gsonString(json)}")

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