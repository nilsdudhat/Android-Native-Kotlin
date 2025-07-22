package com.belive.dating.activities.dashboard.fragments.profile

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.R
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.profile_balances.ProfileBalanceResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.payment.ProductType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "PROFILE_VIEW_MODEL"

    val name = ObservableField<String?>()
    val gender = ObservableField<String?>()
    val age = ObservableField<Int?>()
    val profilePercentage = ObservableField<Int?>()
    val profileImage = ObservableField<String?>()
    val paywallBanners = ArrayList<Int>(listOf(R.drawable.banner_likes, R.drawable.banner_super_likes, R.drawable.banner_boost))

    val activePackage = ObservableField<ProductType>()
    val renewDate = ObservableField<String>()

    val diamonds = ObservableField("0")
    val isUnlimitedRewinds = ObservableField<Boolean?>()
    val rewinds = ObservableField("0")
    val likes = ObservableField("0")
    val isUnlimitedLikes = ObservableField<Boolean?>()
    val superLikes = ObservableField("0")
    val boosts = ObservableField("0")
    val isLifetimeMember = ObservableField<Boolean?>()

    fun updateState() {
        savedStateHandle["${TAG}_diamonds"] = diamonds.get()
        savedStateHandle["${TAG}_isUnlimitedRewinds"] = isUnlimitedRewinds.get()
        savedStateHandle["${TAG}_rewinds"] = rewinds.get()
        savedStateHandle["${TAG}_likes"] = likes.get()
        savedStateHandle["${TAG}_isUnlimitedLikes"] = isUnlimitedLikes.get()
        savedStateHandle["${TAG}_superLikes"] = superLikes.get()
        savedStateHandle["${TAG}_boosts"] = boosts.get()
        savedStateHandle["${TAG}_isLifetimeMember"] = isLifetimeMember.get()
    }

    fun getState() {
        diamonds.set(savedStateHandle["${TAG}_diamonds"])
        isUnlimitedRewinds.set(savedStateHandle["${TAG}_isUnlimitedRewinds"])
        rewinds.set(savedStateHandle["${TAG}_rewinds"])
        likes.set(savedStateHandle["${TAG}_likes"])
        isUnlimitedLikes.set(savedStateHandle["${TAG}_isUnlimitedLikes"])
        superLikes.set(savedStateHandle["${TAG}_superLikes"])
        boosts.set(savedStateHandle["${TAG}_boosts"])
        isLifetimeMember.set(savedStateHandle["${TAG}_isLifetimeMember"])
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun getProfileBalance(): MutableStateFlow<Resource<ProfileBalanceResponse?>> {
        val data = MutableStateFlow<Resource<ProfileBalanceResponse?>>(Resource.loading(null))

        viewModelScope.launch {
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
                            data.emit(Resource.error("Something went wrong...!", null))
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
}