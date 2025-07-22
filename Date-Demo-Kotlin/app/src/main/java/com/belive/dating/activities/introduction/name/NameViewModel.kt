package com.belive.dating.activities.introduction.name

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.introduction.IntroductionRepository
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

class NameViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "NAME_VIEW_MODEL"

    val name = ObservableField("")
    val validationError = ObservableField("")
    val isNextEnabled = ObservableField(false)
    val nameLength = ObservableField(0)

    fun updateState() {
        savedStateHandle["${TAG}_name"] = name.get()
        savedStateHandle["${TAG}_validationError"] = validationError.get()
        savedStateHandle["${TAG}_isNextEnabled"] = isNextEnabled.get()
        savedStateHandle["${TAG}_nameLength"] = nameLength.get()
    }

    fun getState() {
        name.set(savedStateHandle["${TAG}_name"])
        validationError.set(savedStateHandle["${TAG}_validationError"])
        isNextEnabled.set(savedStateHandle["${TAG}_isNextEnabled"])
        nameLength.set(savedStateHandle["${TAG}_nameLength"])
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    private val introductionRepository by lazy {
        getKoinObject().get<IntroductionRepository>()
    }

    fun validateName(): MutableStateFlow<Resource<JsonObject?>> {
        val resource = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = introductionRepository.validateText(name.get()?.trim())
                val errorBody = response.errorBody()?.string()

                logger("--validate_name--", "request.url: ${response.raw().request.url}")
                logger("--validate_name--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--validate_name--", "code: ${response.code()}")
                logger("--validate_name--", "isSuccessful: ${response.isSuccessful}")
                logger("--validate_name--", "errorBody: $errorBody")
                logger("--validate_name--", "body: ${gsonString(response.body())}")

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
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { resource.emit(it) }
            }
        }

        return resource
    }
}