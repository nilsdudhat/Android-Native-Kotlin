package com.belive.dating.activities.dashboard.fragments.message

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.friend.FriendDetails
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MessageViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "MESSAGE_VIEW_MODEL"

    val isDataLoaded = ObservableField(false)
    val friendList = ObservableField<ArrayList<FriendDetails>>()
    val currentSwipedPosition = ObservableField(-1)

    fun updateState() {
        savedStateHandle["${TAG}_isLoading"] = isDataLoaded.get()
        savedStateHandle["${TAG}_friendList"] = gsonString(friendList.get())
        savedStateHandle["${TAG}_currentSwipedPosition"] = currentSwipedPosition.get()
    }

    fun getState() {
        isDataLoaded.set(savedStateHandle["${TAG}_isLoading"])
        friendList.set(savedStateHandle.get<String>("${TAG}_friendList")?.fromJson<ArrayList<FriendDetails>>())
        currentSwipedPosition.set(savedStateHandle["${TAG}_currentSwipedPosition"])
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun blockUser(map: MutableMap<String, Any?>): MutableStateFlow<Resource<JsonObject>> {
        val data = MutableStateFlow<Resource<JsonObject>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.blockUser(map)
                val errorBody = response.errorBody()?.string()

                logger("--block_user--", "url: ${response.raw().request.url}")
                logger("--block_user--", "isSuccessful: " + response.isSuccessful)
                logger("--block_user--", "message: " + response.message())
                logger("--block_user--", "body: " + gsonString(response.body()))
                logger("--block_user--", "errorBody: $errorBody")
                logger("--block_user--", "code: " + response.code())

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