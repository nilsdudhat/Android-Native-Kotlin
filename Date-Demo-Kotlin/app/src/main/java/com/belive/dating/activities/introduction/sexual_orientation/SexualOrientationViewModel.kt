package com.belive.dating.activities.introduction.sexual_orientation

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionRepository
import com.belive.dating.api.introduction.models.SexualOrientationResponse
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

class SexualOrientationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "SEXUAL_ORIENTATION_VIEW_MODEL"

    val isNextEnabled = ObservableField(false)
    val selectedSexuality = ObservableField(-1)
    val isSkeleton = ObservableField(true)

    fun updateState() {
        savedStateHandle["${TAG}_isNextEnabled"] = isNextEnabled.get()
        savedStateHandle["${TAG}_selectedPosition"] = selectedSexuality.get()
        savedStateHandle["${TAG}_isSkeleton"] = isSkeleton.get()
        savedStateHandle["${TAG}_sexualOrientationList"] = gsonString(orientationsResource.value)
    }

    fun getState() {
        isNextEnabled.set(savedStateHandle["${TAG}_isNextEnabled"])
        selectedSexuality.set(savedStateHandle["${TAG}_selectedPosition"])
        isSkeleton.set(savedStateHandle["${TAG}_isSkeleton"])
        orientationsResource.value =
            (savedStateHandle.get<String>("${TAG}_sexualOrientationList")?.fromJson(object : TypeToken<Resource<SexualOrientationResponse?>>() {}))
                ?: Resource.loading(null)
    }

    private val introductionRepository = getKoinObject().get<IntroductionRepository>()

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    val orientationsResource = MutableStateFlow<Resource<SexualOrientationResponse?>>(Resource.loading(null))

    fun getAllSexualOrientations() {
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = introductionRepository.getAllSexualOrientations()
                val errorBody = response.errorBody()?.string()

                logger("--orientations--", "request.url: ${response.raw().request.url}")
                logger("--orientations--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--orientations--", "code: ${response.code()}")
                logger("--orientations--", "isSuccessful: ${response.isSuccessful}")
                logger("--orientations--", "errorBody: $errorBody")
                logger("--orientations--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    if (response.body() != null) {
                        orientationsResource.emit(Resource.success(response.body()))
                    } else {
                        orientationsResource.emit(Resource.error("Something went wrong...!", null))
                    }
                } else {
                    if (!errorBody.isNullOrEmpty()) {
                        orientationsResource.emit(Resource.error(getErrorMessage(errorBody), null))
                    } else {
                        orientationsResource.emit(Resource.error("Something went wrong...!", null))
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { orientationsResource.emit(it) }
            }
        }
    }
}