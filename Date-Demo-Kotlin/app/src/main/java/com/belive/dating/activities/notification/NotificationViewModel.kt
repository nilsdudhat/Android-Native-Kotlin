package com.belive.dating.activities.notification

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.notification.NotificationModel
import com.belive.dating.api.user.models.notification.NotificationResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "NOTIFICATION_VIEW_MODEL"

    val isLoading = ObservableField(true)
    val notificationList = ObservableField<ArrayList<NotificationModel>>(arrayListOf())

    fun updateState() {
        savedStateHandle["${TAG}_isLoading"] = isLoading.get()
        savedStateHandle["${TAG}_month"] = notificationList.get()
    }

    fun getState() {
        isLoading.set(savedStateHandle["${TAG}_isLoading"])
        notificationList.set(savedStateHandle["${TAG}_month"])
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getNotificationHistory(): MutableStateFlow<Resource<NotificationResponse>> {
        val data = MutableStateFlow<Resource<NotificationResponse>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getNotificationHistory()
                val errorBody = response.errorBody()?.string()

                logger("--notification_history--", "request.url: ${response.raw().request.url}")
                logger("--notification_history--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--notification_history--", "code: ${response.code()}")
                logger("--notification_history--", "isSuccessful: ${response.isSuccessful}")
                logger("--notification_history--", "errorBody: $errorBody")
                logger("--notification_history--", "body: ${gsonString(response.body())}")

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
                data.emit(Resource.error("Something went wrong...!", null))
            }
        }

        return data
    }
}