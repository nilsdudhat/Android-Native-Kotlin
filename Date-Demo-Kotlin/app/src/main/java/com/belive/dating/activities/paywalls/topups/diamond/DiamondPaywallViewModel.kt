package com.belive.dating.activities.paywalls.topups.diamond

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.payment.PaymentResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.revenuecat.purchases.Offering
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DiamondPaywallViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "DIAMOND_PAYWALL_VIEW_MODEL"

    val isLoading = ObservableField(true)
    val selectedOffer = ObservableField<Offering>()
    val remainingDiamondsCount = ObservableField<String>()
    val buttonText = ObservableField<String>()
    val diamondPlanList = ObservableField<ArrayList<Offering>>()

    fun updateState() {
        savedStateHandle["${TAG}_selectedOffer"] = gsonString(selectedOffer.get())
        savedStateHandle["${TAG}_remainingDiamondsCount"] = remainingDiamondsCount.get()
        savedStateHandle["${TAG}_buttonText"] = buttonText.get()
        savedStateHandle["${TAG}_diamondPlanList"] = gsonString(diamondPlanList.get())
    }

    fun getState() {
        selectedOffer.set(savedStateHandle.get<String>("${TAG}_selectedOffer")?.fromJson<Offering>())
        remainingDiamondsCount.set(savedStateHandle["${TAG}_remainingDiamondsCount"])
        buttonText.set(savedStateHandle["${TAG}_buttonText"])
        diamondPlanList.set(savedStateHandle.get<String>("${TAG}_diamondPlanList")?.fromJson<ArrayList<Offering>>())
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
}