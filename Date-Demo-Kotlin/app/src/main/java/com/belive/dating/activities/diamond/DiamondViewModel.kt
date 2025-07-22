package com.belive.dating.activities.diamond

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.diamond_question.DiamondQuestionResponse
import com.belive.dating.api.user.models.diamond_question.QuestionData
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

class DiamondViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "DIAMOND_VIEW_MODEL"

    val questionData = ObservableField<QuestionData>()
    val isWatchAdsAvailable = ObservableField(false)
    val isWatchAdsEnabled = ObservableField(false)
    val isDailyCheckInAvailable = ObservableField(false)
    val isDailyCheckInEnabled = ObservableField(false)
    val isDailyOnlineTimeAvailable = ObservableField(false)
    val isDailyOnlineTimeEnabled = ObservableField(false)
    val dailyOnlineTimeSpent = ObservableField<Int>()
    val isDailyOnlineTimeProgress = ObservableField(false)
    val isInviteFriendAvailable = ObservableField(false)
    val isInviteFriendEnabled = ObservableField(false)
    val isRateAppAvailable = ObservableField(false)
    val isRateAppEnabled = ObservableField(false)
    val isCompleteProfileAvailable = ObservableField(false)
    val isCompleteProfileEnabled = ObservableField(false)
    val isWriteAboutMeAvailable = ObservableField(false)
    val isWriteAboutMeEnabled = ObservableField(false)
    val isUploadPhotoAvailable = ObservableField(false)
    val isUploadPhotoEnabled = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_questionData"] = gsonString(questionData.get())
        savedStateHandle["${TAG}_isWatchAdsAvailable"] = isWatchAdsAvailable.get()
        savedStateHandle["${TAG}_isWatchAdsEnabled"] = isWatchAdsEnabled.get()
        savedStateHandle["${TAG}_isDailyCheckInAvailable"] = isDailyCheckInAvailable.get()
        savedStateHandle["${TAG}_isDailyCheckInEnabled"] = isDailyCheckInEnabled.get()
        savedStateHandle["${TAG}_isDailyOnlineTimeAvailable"] = isDailyOnlineTimeAvailable.get()
        savedStateHandle["${TAG}_isDailyOnlineTimeEnabled"] = isDailyOnlineTimeEnabled.get()
        savedStateHandle["${TAG}_isDailyOnlineTimeProgress"] = isDailyOnlineTimeProgress.get()
        savedStateHandle["${TAG}_isInviteFriendAvailable"] = isInviteFriendAvailable.get()
        savedStateHandle["${TAG}_isInviteFriendEnabled"] = isInviteFriendEnabled.get()
        savedStateHandle["${TAG}_isRateAppAvailable"] = isRateAppAvailable.get()
        savedStateHandle["${TAG}_isRateAppEnabled"] = isRateAppEnabled.get()
        savedStateHandle["${TAG}_isCompleteProfileAvailable"] = isCompleteProfileAvailable.get()
        savedStateHandle["${TAG}_isCompleteProfileEnabled"] = isCompleteProfileEnabled.get()
        savedStateHandle["${TAG}_isWriteAboutMeAvailable"] = isWriteAboutMeAvailable.get()
        savedStateHandle["${TAG}_isWriteAboutMeEnabled"] = isWriteAboutMeEnabled.get()
        savedStateHandle["${TAG}_isUploadPhotoAvailable"] = isUploadPhotoAvailable.get()
        savedStateHandle["${TAG}_isUploadPhotoEnabled"] = isUploadPhotoEnabled.get()
    }

    fun getState() {
        questionData.set(savedStateHandle.get<String>("${TAG}_questionData")?.fromJson())
        isWatchAdsAvailable.set(savedStateHandle["${TAG}_isWatchAdsAvailable"])
        isWatchAdsEnabled.set(savedStateHandle["${TAG}_isWatchAdsEnabled"])
        isDailyCheckInAvailable.set(savedStateHandle["${TAG}_isDailyCheckInAvailable"])
        isDailyCheckInEnabled.set(savedStateHandle["${TAG}_isDailyCheckInEnabled"])
        isDailyOnlineTimeAvailable.set(savedStateHandle["${TAG}_isDailyOnlineTimeAvailable"])
        isDailyOnlineTimeEnabled.set(savedStateHandle["${TAG}_isDailyOnlineTimeEnabled"])
        isDailyOnlineTimeProgress.set(savedStateHandle["${TAG}_isDailyOnlineTimeProgress"])
        isInviteFriendAvailable.set(savedStateHandle["${TAG}_isInviteFriendAvailable"])
        isInviteFriendEnabled.set(savedStateHandle["${TAG}_isInviteFriendEnabled"])
        isRateAppAvailable.set(savedStateHandle["${TAG}_isRateAppAvailable"])
        isRateAppEnabled.set(savedStateHandle["${TAG}_isRateAppEnabled"])
        isCompleteProfileAvailable.set(savedStateHandle["${TAG}_isCompleteProfileAvailable"])
        isCompleteProfileEnabled.set(savedStateHandle["${TAG}_isCompleteProfileEnabled"])
        isWriteAboutMeAvailable.set(savedStateHandle["${TAG}_isWriteAboutMeAvailable"])
        isWriteAboutMeEnabled.set(savedStateHandle["${TAG}_isWriteAboutMeEnabled"])
        isUploadPhotoAvailable.set(savedStateHandle["${TAG}_isUploadPhotoAvailable"])
        isUploadPhotoEnabled.set(savedStateHandle["${TAG}_isUploadPhotoEnabled"])
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    fun getDiamondQuestion(): MutableStateFlow<Resource<DiamondQuestionResponse?>> {
        val data = MutableStateFlow<Resource<DiamondQuestionResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getDiamondQuestions()
                val errorBody = response.errorBody()?.string()

                logger("--diamond_questions--", "request.url: ${response.raw().request.url}")
                logger("--diamond_questions--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--diamond_questions--", "code: ${response.code()}")
                logger("--diamond_questions--", "isSuccessful: ${response.isSuccessful}")
                logger("--diamond_questions--", "errorBody: $errorBody")
                logger("--diamond_questions--", "body: ${gsonString(response.body())}")

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
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }

    fun claimDiamond(data: MutableMap<String, Any?>): MutableStateFlow<Resource<JsonObject?>> {
        val claimDiamond = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                claimDiamond.emit(Resource.loading(null))

                val response = userRepository.claimDiamond(data)
                val errorBody = response.errorBody()?.string()

                logger("--claim_diamond--", "request.url: ${response.raw().request.url}")
                logger("--claim_diamond--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--claim_diamond--", "code: ${response.code()}")
                logger("--claim_diamond--", "isSuccessful: ${response.isSuccessful}")
                logger("--claim_diamond--", "errorBody: $errorBody")
                logger("--claim_diamond--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    claimDiamond.emit(Resource.success(response.body()))
                } else if (response.code() == 409) {
                    claimDiamond.emit(Resource.success(JsonObject().apply {
                        addProperty("code", 409)
                        addProperty("data", getErrorMessage(errorBody))
                    }))
                } else {
                    if (response.code() == 401) {
                        claimDiamond.emit(Resource.signOut(getErrorMessage(errorBody), null))
                    } else if (response.code() == 403) {
                        claimDiamond.emit(Resource.adminBlocked(getErrorMessage(errorBody), null))
                    } else {
                        if (!errorBody.isNullOrEmpty()) {
                            claimDiamond.emit(Resource.error(getErrorMessage(errorBody), null))
                        } else {
                            claimDiamond.emit(Resource.error("Something went wrong...!", null))
                        }
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { claimDiamond.emit(it) }
            }
        }

        return claimDiamond
    }
}