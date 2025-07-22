package com.belive.dating.activities.introduction.choose_interest

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionRepository
import com.belive.dating.api.introduction.models.InterestsResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ChooseInterestViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(), KoinComponent {

    val TAG = "CHOOSE_INTEREST_VIEW_MODEL"

    val isSkeleton = ObservableField(true)
    val interestsCount = ObservableField(0)
    val isNextEnabled = ObservableField(false)
    val selectedInterests = ObservableField(ArrayList<Int>())

    fun updateState() {
        savedStateHandle["${TAG}_isSkeleton"] = isSkeleton.get()
        savedStateHandle["${TAG}_interestsCount"] = interestsCount.get()
        savedStateHandle["${TAG}_isNextEnabled"] = isNextEnabled.get()
        savedStateHandle["${TAG}_selectedInterests"] = selectedInterests.get()
        savedStateHandle["${TAG}_interestList"] = gsonString(interestsResource.value)
    }

    fun getState() {
        isSkeleton.set(savedStateHandle["${TAG}_isSkeleton"])
        interestsCount.set(savedStateHandle["${TAG}_interestsCount"])
        isNextEnabled.set(savedStateHandle["${TAG}_isNextEnabled"])
        selectedInterests.set(savedStateHandle["${TAG}_selectedInterests"])
        interestsResource.value =
            (savedStateHandle.get<String>("${TAG}_interestList")?.fromJson(object : TypeToken<Resource<InterestsResponse?>>() {}))
                ?: Resource.loading(null)
    }

    private val introductionRepository = getKoinObject().get<IntroductionRepository>()

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    val interestsResource = MutableStateFlow<Resource<InterestsResponse?>>(Resource.loading(null))

    fun getAllInterests() {
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = introductionRepository.getAllInterests()
                val errorBody = response.errorBody()?.string()

                logger("--interests--", "request.url: ${response.raw().request.url}")
                logger("--interests--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--interests--", "code: ${response.code()}")
                logger("--interests--", "isSuccessful: ${response.isSuccessful}")
                logger("--interests--", "errorBody: $errorBody")
                logger("--interests--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    if (response.body() != null) {
                        interestsResource.emit(Resource.success(response.body()))
                    } else {
                        interestsResource.emit(Resource.error("Something went wrong...!", null))
                    }
                } else {
                    if (!errorBody.isNullOrEmpty()) {
                        interestsResource.emit(Resource.error(getErrorMessage(errorBody), null))
                    } else {
                        interestsResource.emit(Resource.error("Something went wrong...!", null))
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { interestsResource.emit(it) }
            }
        }
    }
}