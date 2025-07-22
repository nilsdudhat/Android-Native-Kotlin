package com.belive.dating.activities.dashboard.fragments.home

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.diamond_question.DiamondQuestionResponse
import com.belive.dating.api.user.models.diamond_question.QuestionData
import com.belive.dating.api.user.models.home.RewindDeductResponse
import com.belive.dating.api.user.models.home_profiles.HomeUsersResponse
import com.belive.dating.api.user.models.home_profiles.User
import com.belive.dating.api.user.models.like_profile.LikeProfileResponse
import com.belive.dating.api.user.models.profile_balances.ProfileBalanceResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.helpers.helper_views.card_swiper.Direction
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class HomeViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(), KoinComponent {

    val isDataLoaded = ObservableField(false)
    val isLoadingProfiles = ObservableField(false)
    val questionData = ObservableField<QuestionData>()
    val homeUserList = ObservableField<ArrayList<User>?>()
    val isBoostEnabled = ObservableField(false)
    val boostProgress = ObservableField<Long>()
    val topPosition = MutableStateFlow<Int>(0)
    val dragState = MutableStateFlow<Pair<Int, Direction?>>(Pair(-1, null))

    fun updateState() {
        savedStateHandle["isDataLoaded"] = isDataLoaded.get()
        savedStateHandle["homeUserList"] = homeUserList.get()
        savedStateHandle["questionData"] = questionData.get()
    }

    fun getState() {
        isDataLoaded.set(savedStateHandle["isDataLoaded"] ?: false)
        homeUserList.set(savedStateHandle["homeUserList"])
        questionData.set(savedStateHandle["questionData"])
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun skipProfile(profileID: Int) {
        job = viewModelScope.launch {
            val response = userRepository.skipProfile(profileID)
            val errorBody = response.errorBody()?.string()

            logger("--skip_profile--", "request.url: ${response.raw().request.url}")
            logger("--skip_profile--", "request.body: ${gsonString(response.raw().request.body)}")
            logger("--skip_profile--", "code: ${response.code()}")
            logger("--skip_profile--", "isSuccessful: ${response.isSuccessful}")
            logger("--skip_profile--", "errorBody: $errorBody")
            logger("--skip_profile--", "body: ${gsonString(response.body())}")
            logger("--skip_profile--", "profileID: $profileID")
            logger("-------check-------", "name: ${homeUserList.get()?.find { it.id == profileID }?.fullName}")
        }
    }

    fun likeProfile(profileID: Int, likeType: Int): MutableStateFlow<Resource<LikeProfileResponse?>> {
        val data = MutableStateFlow<Resource<LikeProfileResponse?>>(Resource.loading(null))

        job = viewModelScope.launch {
            try {
                val queryJson = JsonObject()
                queryJson.addProperty("profile_id", profileID)
                queryJson.addProperty("like_type", likeType)

                val response = userRepository.likeProfile(queryJson)
                val errorBody = response.errorBody()?.string()

                logger("--like_profile--", "request.url: ${response.raw().request.url}")
                logger("--like_profile--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--like_profile--", "code: ${response.code()}")
                logger("--like_profile--", "isSuccessful: ${response.isSuccessful}")
                logger("--like_profile--", "errorBody: $errorBody")
                logger("--like_profile--", "body: ${gsonString(response.body())}")
                logger("--like_profile--", "profileID: $profileID")
                logger("-------check-------", "name: ${homeUserList.get()?.find { it.id == profileID }?.fullName}")

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

    fun deductRewind(profileID: Int, deductFrom: String?): MutableStateFlow<Resource<RewindDeductResponse?>> {
        val data = MutableStateFlow<Resource<RewindDeductResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.deductRewind(profileID, deductFrom)
                val errorBody = response.errorBody()?.string()

                logger("--deduct_rewind--", "request.url: ${response.raw().request.url}")
                logger("--deduct_rewind--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--deduct_rewind--", "code: ${response.code()}")
                logger("--deduct_rewind--", "isSuccessful: ${response.isSuccessful}")
                logger("--deduct_rewind--", "errorBody: $errorBody")
                logger("--deduct_rewind--", "body: ${gsonString(response.body())}")
                logger("-------check-------", "name: ${homeUserList.get()?.find { it.id == profileID }?.fullName}")

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

    fun deductBoost(): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.deductBoost()
                val errorBody = response.errorBody()?.string()

                logger("--deduct_boost--", "request.url: ${response.raw().request.url}")
                logger("--deduct_boost--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--deduct_boost--", "code: ${response.code()}")
                logger("--deduct_boost--", "isSuccessful: ${response.isSuccessful}")
                logger("--deduct_boost--", "errorBody: $errorBody")
                logger("--deduct_boost--", "body: ${gsonString(response.body())}")

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

    fun getHomeProfiles(countryCode: String, remainingList: MutableList<Int>? = null): MutableStateFlow<Resource<HomeUsersResponse?>> {
        val data = MutableStateFlow<Resource<HomeUsersResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val initTime = System.currentTimeMillis()
                val ignoreIds = remainingList?.joinToString(",") { it.toString() }

                val response = userRepository.getHomeProfiles(countryCode, ignoreIds)
                val errorBody = response.errorBody()?.string()

                logger("--home_profiles--", "loadingTime: ${(System.currentTimeMillis() - initTime)}")
                logger("--home_profiles--", "request.url: ${response.raw().request.url}")
                logger("--home_profiles--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--home_profiles--", "code: ${response.code()}")
                logger("--home_profiles--", "isSuccessful: ${response.isSuccessful}")
                logger("--home_profiles--", "errorBody: $errorBody")
                logger("--home_profiles--", "query: ${response.raw().request.url.query}")
                logger("--home_profiles--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    val list = ArrayList(data.value.data?.userList ?: arrayListOf())
                    response.body()?.userList?.addAll(list)
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

    fun getProfileBalance(): MutableStateFlow<Resource<ProfileBalanceResponse?>> {
        val data = MutableStateFlow<Resource<ProfileBalanceResponse?>>(Resource.loading(null))

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getProfileBalance()
                val errorBody = response.errorBody()?.string()

                logger("--profile_balance--", "request.url: ${response.raw().request.url}")
                logger("--profile_balance--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--profile_balance--", "code: ${response.code()}")
                logger("--profile_balance--", "isSuccessful: ${response.isSuccessful}")
                logger("--profile_balance--", "errorBody: $errorBody")
                logger("--profile_balance--", "body: ${gsonString(response.body())}")

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
                            data.emit(Resource.error("Something went wrong", null))
                        }
                    }
                }
            } catch (e: Exception) {
                logger("--profile_balance--", "catch: ${gsonString(e)}")

                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }

    fun getDiamondQuestion(): MutableStateFlow<Resource<DiamondQuestionResponse?>> {
        val data = MutableStateFlow<Resource<DiamondQuestionResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getDiamondQuestions()
                val errorBody = response.errorBody()?.string()

                logger("--diamond_questions--", "request.url: ${response.raw().request.url}")
                logger("--diamond_questions--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--diamond_questions--", "code: ${response.code()}")
                logger("--diamond_questions--", "isSuccessful: ${response.isSuccessful}")
                logger("--diamond_questions--", "errorBody: $errorBody")
                logger("--diamond_questions--", "body: ${gsonString(response.body())}")

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
                            data.emit(Resource.error("Something went wrong", null))
                        }
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Resource.error(it, null) }?.let { data.emit(it) }
            }
        }

        return data
    }

    fun claimDiamond(diamondClaimPayload: MutableMap<String, Any?>): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.claimDiamond(diamondClaimPayload)
                val errorBody = response.errorBody()?.string()

                logger("--claim_diamond--", "request.url: ${response.raw().request.url}")
                logger("--claim_diamond--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--claim_diamond--", "code: ${response.code()}")
                logger("--claim_diamond--", "isSuccessful: ${response.isSuccessful}")
                logger("--claim_diamond--", "errorBody: $errorBody")
                logger("--claim_diamond--", "body: ${gsonString(response.body())}")

                if (response.isSuccessful) {
                    data.emit(Resource.success(response.body()))
                } else if (response.code() == 409) {
                    data.emit(Resource.success(JsonObject().apply {
                        addProperty("code", 409)
                        addProperty("data", getErrorMessage(errorBody))
                    }))
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

    private val bitmapCache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt() // Use 1/8th of available memory
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024 // Measure size in kilobytes
        }
    }

    // Add bitmap to cache
    fun addBitmapToCache(imagePath: String, bitmap: Bitmap) {
        synchronized(this::addBitmapToCache) {
            logger("--lru--", bitmapCache.size())
            if (bitmapCache[imagePath] == null) {
                bitmapCache.put(imagePath, bitmap)
            }
        }
    }

    // Retrieve bitmap from cache
    fun getBitmapFromCache(imagePath: String): Bitmap? {
        synchronized(this::addBitmapToCache) {
            val bitmap = bitmapCache[imagePath]
            return bitmap
        }
    }
}