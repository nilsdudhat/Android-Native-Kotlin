package com.belive.dating.activities.settings.manage_notifications

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.notification_settings.NotificationSettings
import com.belive.dating.api.user.models.notification_settings.NotificationSettingsResponse
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

class ManageNotificationsViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val showNewMatchNotifications = ObservableField(true)
    val showNewMessageNotifications = ObservableField(true)
    val showNewLikeNotifications = ObservableField(true)
    val showNewSuperLikeNotifications = ObservableField(true)

    var notificationSettings = ObservableField<NotificationSettings>()

    val isButtonEnabled = ObservableField(false)

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    fun getAlertNotificationSettings(): MutableStateFlow<Resource<NotificationSettingsResponse?>> {
        val data = MutableStateFlow<Resource<NotificationSettingsResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getNotificationSettings()
                val errorBody = response.errorBody()?.string()

                logger("--notification_settings--", "request.url: ${response.raw().request.url}")
                logger("--notification_settings--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--notification_settings--", "code: ${response.code()}")
                logger("--notification_settings--", "isSuccessful: ${response.isSuccessful}")
                logger("--notification_settings--", "errorBody: $errorBody")
                logger("--notification_settings--", "body: ${gsonString(response.body())}")

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

    fun updateNotificationSettings(): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = JsonObject()
                json.addProperty("new_match_alert", showNewMatchNotifications.get())
                json.addProperty("new_message_alert", showNewMessageNotifications.get())
                json.addProperty("new_like_alert", showNewLikeNotifications.get())
                json.addProperty("new_super_like_alert", showNewSuperLikeNotifications.get())

                val response = userRepository.updateNotificationSettings(json)
                val errorBody = response.errorBody()?.string()

                logger("--update_notification_settings--", "request.url: ${response.raw().request.url}")
                logger("--update_notification_settings--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--update_notification_settings--", "code: ${response.code()}")
                logger("--update_notification_settings--", "isSuccessful: ${response.isSuccessful}")
                logger("--update_notification_settings--", "errorBody: $errorBody")
                logger("--update_notification_settings--", "body: ${gsonString(response.body())}")
                logger("--update_notification_settings--", "query: ${gsonString(json)}")

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
}