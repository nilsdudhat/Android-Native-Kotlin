package com.belive.dating.activities.profile

import android.text.SpannableString
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

class ProfileViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "PROFILE_VIEW_MODEL"

    val profilePercentageTitle = ObservableField<SpannableString?>()

    val photoList = ObservableField(ArrayList<String?>())
    val photosCount = ObservableField<Int?>()

    val name = ObservableField<String?>()
    val birthDate = ObservableField<String?>()
    val gender = ObservableField<String?>()
    val isIncompletePersonalDetails = ObservableField(false)

    val height = ObservableField<String?>()

    val aboutMe = ObservableField<String?>()

    val livingIn = ObservableField<String?>()

    val hideAge = ObservableField(false)
    val hideDistance = ObservableField(false)

    val relationshipGoal = ObservableField<String?>()

    val oppositeGender = ObservableField<String?>()

    val sexualOrientation = ObservableField<String?>()

    val school = ObservableField<String?>()

    val languagesCount = ObservableField<Int?>()

    val zodiac = ObservableField<String?>()
    val education = ObservableField<String?>()
    val religion = ObservableField<String?>()
    val maritalStatus = ObservableField<String?>()
    val familyPlan = ObservableField<String?>()
    val isIncompleteBasics = ObservableField(false)
    val isShowBasicsPercentage = ObservableField(true)

    val personalityCount = ObservableField<Int?>()
    val communicationCount = ObservableField<Int?>()
    val loveTypeCount = ObservableField<Int?>()
    val isIncompleteStyle = ObservableField(false)
    val isShowStylePercentage = ObservableField(true)

    val interestsCount = ObservableField<Int?>()

    val pet = ObservableField<String?>()
    val drinking = ObservableField<String?>()
    val smoking = ObservableField<String?>()
    val workout = ObservableField<String?>()
    val preferredDiet = ObservableField<String?>()
    val socialMedia = ObservableField<String?>()
    val sleepingHabit = ObservableField<String?>()
    val isIncompleteLifestyle = ObservableField(false)
    val isShowLifestylePercentage = ObservableField(true)

    fun updateState() {
        savedStateHandle["${TAG}_profilePercentageTitle"] = profilePercentageTitle.get()
        savedStateHandle["${TAG}_photoList"] = photoList.get()
        savedStateHandle["${TAG}_photosCount"] = photosCount.get()
        savedStateHandle["${TAG}_aboutMe"] = aboutMe.get()
        savedStateHandle["${TAG}_zodiac"] = zodiac.get()
        savedStateHandle["${TAG}_education"] = education.get()
        savedStateHandle["${TAG}_oppositeGender"] = oppositeGender.get()
        savedStateHandle["${TAG}_sexualOrientation"] = sexualOrientation.get()
        savedStateHandle["${TAG}_relationshipGoal"] = relationshipGoal.get()
        savedStateHandle["${TAG}_interests"] = interestsCount.get()
    }

    fun getState() {
        profilePercentageTitle.set(savedStateHandle["${TAG}_profilePercentageTitle"])
        photoList.set(savedStateHandle["${TAG}_photoList"])
        photosCount.set(savedStateHandle["${TAG}_photosCount"])
        aboutMe.set(savedStateHandle["${TAG}_aboutMe"])
        zodiac.set(savedStateHandle["${TAG}_zodiac"])
        education.set(savedStateHandle["${TAG}_education"])
        oppositeGender.set(savedStateHandle["${TAG}_oppositeGender"])
        sexualOrientation.set(savedStateHandle["${TAG}_sexualOrientation"])
        relationshipGoal.set(savedStateHandle["${TAG}_relationshipGoal"])
        interestsCount.set(savedStateHandle["${TAG}_interests"])
    }

    val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun saveProfileChanges(): MutableStateFlow<Resource<UserResponse?>> {
        val data = MutableStateFlow<Resource<UserResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {

            val json = JsonObject()
            json.addProperty("dont_show_my_age", hideAge.get())
            json.addProperty("dont_show_my_distance", hideDistance.get())

            val response = userRepository.updateUserDetails(json)
            val errorBody = response.errorBody()?.string()

            logger("--update_profile_changes--", "request.url: ${response.raw().request.url}")
            logger("--update_profile_changes--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--update_profile_changes--", "code: ${response.code()}")
            logger("--update_profile_changes--", "isSuccessful: ${response.isSuccessful}")
            logger("--update_profile_changes--", "errorBody: $errorBody")
            logger("--update_profile_changes--", "body: ${gsonString(response.body())}")
            logger("--update_profile_changes--", "query: ${gsonString(json)}")

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