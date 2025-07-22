package com.belive.dating.activities.settings

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.constants.PROFILE_VISIBILITY
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val isPremiumAvailable = ObservableField(false)
    val profileVisibilityType = ObservableField(PROFILE_VISIBILITY.SLANDERED.profileVisibilityIndex)

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun logout(): MutableStateFlow<Resource<JsonObject>> {
        val data = MutableStateFlow<Resource<JsonObject>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.logout()
                val errorBody = response.errorBody()?.string()

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

    fun deleteUser(): MutableStateFlow<Resource<JsonObject>> {
        val data = MutableStateFlow<Resource<JsonObject>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.deleteUser()
                val errorBody = response.errorBody()?.string()

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