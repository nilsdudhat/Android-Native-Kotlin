package com.belive.dating.activities.dashboard.fragments.ls.fragments.super_like

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.liked_me_profiles.LikedMeProfile
import com.belive.dating.api.user.models.liked_me_profiles.LikedMeProfilesResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SuperLikeViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "SUPER_LIKE_VIEW_MODEL"

    val isLoading = ObservableField(false)
    val isDataLoaded = ObservableField(false)
    val superLikedMeProfileList = MutableStateFlow<ArrayList<LikedMeProfile>?>(null)

    private var pageNumber: Int = 0
    private var totalPage: Int = 0

    fun updateState() {
        savedStateHandle["${TAG}_isDataLoaded"] = isDataLoaded.get()
        savedStateHandle["${TAG}_pageStart"] = pageNumber
        savedStateHandle["${TAG}_totalPage"] = totalPage
        savedStateHandle["${TAG}_isLoading"] = isLoading.get()
    }

    fun getState() {
        isDataLoaded.set(savedStateHandle["${TAG}_isDataLoaded"] ?: false)
        pageNumber = savedStateHandle["${TAG}_pageStart"] ?: 0
        totalPage = savedStateHandle["${TAG}_totalPage"] ?: 0
        isLoading.set(savedStateHandle["${TAG}_isLoading"] ?: false)
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getSuperLikeProfiles(countryCode: String): MutableStateFlow<Resource<LikedMeProfilesResponse?>> {
        if (pageNumber < totalPage) {
            pageNumber++
        }

        val data = MutableStateFlow<Resource<LikedMeProfilesResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getLikeProfiles(pageNumber, countryCode, "superlike")
                val errorBody = response.errorBody()?.string()

                logger("--super_like_profiles--", "request.url: ${response.raw().request.url}")
                logger("--super_like_profiles--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--super_like_profiles--", "code: ${response.code()}")
                logger("--super_like_profiles--", "isSuccessful: ${response.isSuccessful}")
                logger("--super_like_profiles--", "errorBody: $errorBody")
                logger("--super_like_profiles--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    pageNumber = response.body()?.pagination?.currentPage ?: 0
                    totalPage = response.body()?.pagination?.pageCount ?: 0

                    val list = ArrayList(data.value.data?.likedMeProfileList ?: arrayListOf())
                    response.body()?.likedMeProfileList?.addAll(list)
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