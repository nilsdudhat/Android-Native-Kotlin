package com.belive.dating.activities.user_details

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.like_profile.LikeProfileResponse
import com.belive.dating.api.user.models.user_details.UserDetails
import com.belive.dating.api.user.models.user_details.UserDetailsResponse
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

class UserDetailsViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "USER_DETAILS_VIEW_MODEL"

    val userDetails = ObservableField<UserDetails?>()

    fun updateState() {
        savedStateHandle["${TAG}_userDetails"] = userDetails.get()
    }

    fun getState() {
        userDetails.set(savedStateHandle["${TAG}_userDetails"])
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
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

    fun getUserDetails(userId: Int): MutableStateFlow<Resource<UserDetailsResponse>> {
        val data = MutableStateFlow<Resource<UserDetailsResponse>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getUserDetails(userId)
                val errorBody = response.errorBody()?.string()

                logger("--user_details--", "url: ${response.raw().request.url}")
                logger("--user_details--", "isSuccessful: " + response.isSuccessful)
                logger("--user_details--", "message: " + response.message())
                logger("--user_details--", "body: " + gsonString(response.body()))
                logger("--user_details--", "errorBody: $errorBody")
                logger("--user_details--", "code: " + response.code())

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

    fun skipProfile(): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch {
            val response = userRepository.skipProfile(userDetails.get()!!.id)
            val errorBody = response.errorBody()?.string()

            logger("--skip_profile--", "request.url: ${response.raw().request.url}")
            logger("--skip_profile--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--skip_profile--", "code: ${response.code()}")
            logger("--skip_profile--", "isSuccessful: ${response.isSuccessful}")
            logger("--skip_profile--", "errorBody: $errorBody")
            logger("--skip_profile--", "body: ${gsonString(response.body())}")
            logger("--skip_profile--", "profileID: ${userDetails.get()!!.id}")

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
        }

        return data
    }

    fun likeProfile(likeType: Int): MutableStateFlow<Resource<LikeProfileResponse?>> {
        val data = MutableStateFlow<Resource<LikeProfileResponse?>>(Resource.loading(null))

        job = viewModelScope.launch {
            try {
                val queryJson = JsonObject()
                queryJson.addProperty("profile_id", userDetails.get()!!.id)
                queryJson.addProperty("like_type", likeType)

                val response = userRepository.likeProfile(queryJson)
                val errorBody = response.errorBody()?.string()

                logger("--like_profile--", "request.url: ${response.raw().request.url}")
                logger("--like_profile--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--like_profile--", "code: ${response.code()}")
                logger("--like_profile--", "isSuccessful: ${response.isSuccessful}")
                logger("--like_profile--", "errorBody: $errorBody")
                logger("--like_profile--", "body: ${gsonString(response.body())}")
                logger("--like_profile--", "profileID: ${userDetails.get()!!.id}")

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
                logger("--like_profile--", "catch: ${gsonString(e)}")

                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }

    fun readProfile(): MutableStateFlow<Resource<JsonObject>> {
        val data = MutableStateFlow<Resource<JsonObject>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.readProfile(userDetails.get()!!.id)
                val errorBody = response.errorBody()?.string()

                logger("--read_profile--", "request.url: ${response.raw().request.url}")
                logger("--read_profile--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--read_profile--", "code: ${response.code()}")
                logger("--read_profile--", "isSuccessful: ${response.isSuccessful}")
                logger("--read_profile--", "errorBody: $errorBody")
                logger("--read_profile--", "body: ${gsonString(response.body())}")

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
                logger("--catch--", "readProfile: ${gsonString(e)}")
            }
        }

        return data
    }

    fun readNotification(notificationId: Int): MutableStateFlow<Resource<JsonObject>> {
        val data = MutableStateFlow<Resource<JsonObject>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.readNotification(notificationId)
                val errorBody = response.errorBody()?.string()

                logger("--notification--", "url: ${response.raw().request.url}")
                logger("--notification--", "isSuccessful: " + response.isSuccessful)
                logger("--notification--", "message: " + response.message())
                logger("--notification--", "body: " + gsonString(response.body()))
                logger("--notification--", "errorBody: $errorBody")
                logger("--notification--", "code: " + response.code())

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