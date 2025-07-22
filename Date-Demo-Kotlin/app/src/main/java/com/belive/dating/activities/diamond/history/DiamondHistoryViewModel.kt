package com.belive.dating.activities.diamond.history

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.diamond_history.DiamondHistoryResponse
import com.belive.dating.api.user.models.diamond_history.DiamondTransaction
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DiamondHistoryViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "DIAMOND_VIEW_MODEL"

    val diamondHistoryList = ObservableField<ArrayList<DiamondTransaction>>(arrayListOf())
    val isLoading = ObservableField(true)

    private var pageNumber: Int = 1
    private var totalPage: Int = 0

    fun updateState() {
        savedStateHandle["${TAG}_diamondHistoryList"] = diamondHistoryList.get()
        savedStateHandle["${TAG}_pageNumber"] = pageNumber
        savedStateHandle["${TAG}_totalPage"] = totalPage
    }

    fun getState() {
        diamondHistoryList.set(savedStateHandle["${TAG}_diamondHistoryList"])
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

    fun getDiamondHistory(): MutableStateFlow<Resource<DiamondHistoryResponse?>> {
        val data = MutableStateFlow<Resource<DiamondHistoryResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                if ((totalPage != 0) && (pageNumber > totalPage)) {
                    data.emit(Resource.success(null))
                    return@launch
                }

                val response = userRepository.getDiamondHistory(pageNumber, 10)
                val errorBody = response.errorBody()?.string()

                logger("--diamond_history--", "request.url: ${response.raw().request.url}")
                logger("--diamond_history--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--diamond_history--", "code: ${response.code()}")
                logger("--diamond_history--", "isSuccessful: ${response.isSuccessful}")
                logger("--diamond_history--", "errorBody: $errorBody")
                logger("--diamond_history--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    pageNumber = (response.body()?.pagination?.currentPage ?: 1) + 1
                    totalPage = response.body()?.pagination?.pageCount ?: 0

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
                            data.emit(Resource.error("Something went wrong", null))
                        }
                    }
                }
            } catch (e: Exception) {
                data.emit(Resource.error(e.message ?: "Something went wrong", null))
            }
        }

        return data
    }
}