package com.belive.dating.activities.paywalls.topups.rewind

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.payment.PaymentResponse
import com.belive.dating.api.user.models.rewind_like_conversion.RewindLikeConversionResponse
import com.belive.dating.api.user.models.rewind_like_conversion.RewindLikeModel
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.gson.JsonObject
import com.revenuecat.purchases.Offering
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RewindPaywallViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "REWIND_PAYWALL_VIEW_MODEL"

    val isLoading = ObservableField(true)
    val rewindLikeModel = ObservableField<RewindLikeModel>()
    val isUnlimitedRewinds = ObservableField<Boolean>()
    val selectedOffer = ObservableField<Offering>()
    val remainingRewindsCount = ObservableField<String>()
    val buttonText = ObservableField<String>()
    val rewindPlanList = ObservableField<ArrayList<Offering>>()

    fun updateState() {
        savedStateHandle["${TAG}_rewindLikeModel"] = gsonString(rewindLikeModel.get())
        savedStateHandle["${TAG}_isUnlimitedRewinds"] = isUnlimitedRewinds.get()
        savedStateHandle["${TAG}_selectedOffer"] = gsonString(selectedOffer.get())
        savedStateHandle["${TAG}_remainingRewindsCount"] = remainingRewindsCount.get()
        savedStateHandle["${TAG}_buttonText"] = buttonText.get()
        savedStateHandle["${TAG}_rewindPlanList"] = gsonString(rewindPlanList.get())
    }

    fun getState() {
        rewindLikeModel.set(savedStateHandle.get<String>("${TAG}_rewindLikeModel")?.fromJson<RewindLikeModel>())
        isUnlimitedRewinds.set(savedStateHandle["${TAG}_isUnlimitedRewinds"])
        selectedOffer.set(savedStateHandle.get<String>("${TAG}_selectedOffer")?.fromJson<Offering>())
        remainingRewindsCount.set(savedStateHandle["${TAG}_remainingRewindsCount"])
        buttonText.set(savedStateHandle["${TAG}_buttonText"])
        rewindPlanList.set(savedStateHandle.get<String>("${TAG}_rewindPlanList")?.fromJson<ArrayList<Offering>>())
    }

    private val userRepository by lazy {
        getKoinObject().get<UserRepository>()
    }

    private var job: Job? = null

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }

    fun makePurchase(map: MutableMap<String, Any?>): MutableStateFlow<Resource<PaymentResponse?>> {
        val data = MutableStateFlow<Resource<PaymentResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.makePurchase(map)
                val errorBody = response.errorBody()?.string()

                logger("--make_purchase--", "request.url: ${response.raw().request.url}")
                logger("--make_purchase--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--make_purchase--", "code: ${response.code()}")
                logger("--make_purchase--", "isSuccessful: ${response.isSuccessful}")
                logger("--make_purchase--", "errorBody: $errorBody")
                logger("--make_purchase--", "body: ${gsonString(response.body())}")

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
                logger("--make_purchase--", "body: ${gsonString(e)}")

                data.emit(Resource.error("Something went wrong...!", null))
            }
        }

        return data
    }

    fun getRewindLikeConversionDetails(): MutableStateFlow<Resource<RewindLikeConversionResponse?>> {
        val data = MutableStateFlow<Resource<RewindLikeConversionResponse?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.getRewindLikeConversionDetails("rewind")
                val errorBody = response.errorBody()?.string()

                logger("--diamond_conversion--", "request.url: ${response.raw().request.url}")
                logger("--diamond_conversion--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--diamond_conversion--", "code: ${response.code()}")
                logger("--diamond_conversion--", "isSuccessful: ${response.isSuccessful}")
                logger("--diamond_conversion--", "errorBody: $errorBody")
                logger("--diamond_conversion--", "body: ${gsonString(response.body())}")

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
                logger("--make_purchase--", "body: ${gsonString(e)}")

                data.emit(Resource.error("Something went wrong...!", null))
            }
        }

        return data
    }

    fun redeemRewindLike(map: MutableMap<String, Any?>): MutableStateFlow<Resource<JsonObject?>> {
        val data = MutableStateFlow<Resource<JsonObject?>>(Resource.loading(null))

        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.redeemRewindLike(map)
                val errorBody = response.errorBody()?.string()

                logger("--redeem_rewind_like--", "request.url: ${response.raw().request.url}")
                logger("--redeem_rewind_like--", "request.body: ${gsonString(response.raw().request.body)}")
                logger("--redeem_rewind_like--", "code: ${response.code()}")
                logger("--redeem_rewind_like--", "isSuccessful: ${response.isSuccessful}")
                logger("--redeem_rewind_like--", "errorBody: $errorBody")
                logger("--redeem_rewind_like--", "body: ${gsonString(response.body())}")

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
                logger("--make_purchase--", "body: ${gsonString(e)}")

                data.emit(Resource.error("Something went wrong...!", null))
            }
        }

        return data
    }
}