package com.belive.dating.activities.dashboard.fragments.ai

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.ai.AIProfile
import com.belive.dating.api.user.models.ai.AIProfilesResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AIViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "AI_VIEW_MODEL"

    val isAIMatchAvailable = ObservableField(false)
    val isLoading = ObservableField(false)
    val isDataLoaded = ObservableField(false)
    val aiProfileList = MutableStateFlow<ArrayList<AIProfile>?>(null)

    var pageNumber: Int = 1
    var totalPage: Int = 0

    fun updateState() {
        savedStateHandle["${TAG}_isAIMatchAvailable"] = isAIMatchAvailable.get()
        savedStateHandle["${TAG}_isDataLoaded"] = isDataLoaded.get()
        savedStateHandle["${TAG}_pageNumber"] = pageNumber
        savedStateHandle["${TAG}_totalPage"] = totalPage
    }

    fun getState() {
        isAIMatchAvailable.set(savedStateHandle["${TAG}_isAIMatchAvailable"] ?: false)
        isDataLoaded.set(savedStateHandle["${TAG}_isDataLoaded"] ?: false)
        pageNumber = savedStateHandle["${TAG}_pageNumber"] ?: 1
        totalPage = savedStateHandle["${TAG}_totalPage"] ?: 0
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getAIProfiles(countryCode: String): MutableStateFlow<Resource<AIProfilesResponse?>> {
        val data = MutableStateFlow<Resource<AIProfilesResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                if ((totalPage != 0) && (pageNumber > totalPage)) {
                    data.emit(Resource.success(null))
                    return@launch
                }

                val response = userRepository.getAIProfiles(pageNumber, countryCode, "match")
                val errorBody = response.errorBody()?.string()

                logger("--ai_profiles--", "request.url: ${response.raw().request.url}")
                logger("--ai_profiles--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--ai_profiles--", "code: ${response.code()}")
                logger("--ai_profiles--", "isSuccessful: ${response.isSuccessful}")
                logger("--ai_profiles--", "errorBody: $errorBody")
                logger("--ai_profiles--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    pageNumber = (response.body()?.pagination?.currentPage ?: 1) + 1
                    totalPage = response.body()?.pagination?.pageCount ?: 0

                    val list = ArrayList(data.value.data?.aiProfileList ?: arrayListOf())
                    response.body()?.aiProfileList?.addAll(list)
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
}