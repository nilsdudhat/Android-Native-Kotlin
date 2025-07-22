package com.belive.dating.activities.paywalls.subscriptions.subscription

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belive.dating.R
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.models.payment.PaymentResponse
import com.belive.dating.extensions.Resource
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getErrorMessage
import com.belive.dating.extensions.getKoinObject
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.payment.PlanFeatureModel
import com.belive.dating.payment.ProductType
import com.revenuecat.purchases.Offering
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "SUBSCRIPTION_VIEW_MODEL"

    val isLoading = ObservableField(true)
    val isGoldNotAvailable = ObservableField(true)
    val selectedPlanType = ObservableField(ProductType.GOLD)
    val selectedPlan = ObservableField<MutableMap<ProductType, Offering>>()
    val buttonText = ObservableField<String?>()
    val goldPlanList = ObservableField<ArrayList<Offering>>()
    val platinumPlanList = ObservableField<ArrayList<Offering>>()
    val lifetimePlan = ObservableField<Offering>()

    fun updateState() {
        savedStateHandle["${TAG}_isGoldNotAvailable"] = isGoldNotAvailable.get()
        savedStateHandle["${TAG}_selectedPlanType"] = selectedPlanType.get()
        savedStateHandle["${TAG}_selectedPlan"] = gsonString(selectedPlan.get())
        savedStateHandle["${TAG}_buttonText"] = buttonText.get()
        savedStateHandle["${TAG}_goldPlanList"] = gsonString(goldPlanList.get())
        savedStateHandle["${TAG}_platinumPlanList"] = gsonString(platinumPlanList.get())
        savedStateHandle["${TAG}_lifetimePlan"] = gsonString(lifetimePlan.get())
    }

    fun getState() {
        isGoldNotAvailable.set(savedStateHandle["${TAG}_isGoldNotAvailable"])
        selectedPlanType.set(savedStateHandle["${TAG}_selectedPlanType"])
        selectedPlan.set(savedStateHandle.get<String>("${TAG}_selectedPlan")?.fromJson<MutableMap<ProductType, Offering>>())
        buttonText.set(savedStateHandle["${TAG}_buttonText"])
        goldPlanList.set(savedStateHandle.get<String>("${TAG}_goldPlanList")?.fromJson<ArrayList<Offering>>())
        platinumPlanList.set(savedStateHandle.get<String>("${TAG}_platinumPlanList")?.fromJson<ArrayList<Offering>>())
        lifetimePlan.set(savedStateHandle.get<String>("${TAG}_lifetimePlan")?.fromJson<Offering>())
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

    fun getGoldFeatureList(): ArrayList<PlanFeatureModel> {
        val list = arrayListOf<PlanFeatureModel>()

        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_message_unselected,
                title = "Unlock Messaging",
                titleColor = R.color.white,
                tickColor = R.color.gold_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_like_infi,
                title = "Unlimited Likes",
                titleColor = R.color.white,
                tickColor = R.color.gold_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_rewinds_infi,
                title = "Unlimited Rewinds",
                titleColor = R.color.white,
                tickColor = R.color.gold_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_diamond,
                highlight = "500",
                highlightColor = R.color.diamond_color,
                title = "Diamonds per Month",
                titleColor = R.color.white,
                subTitle = "(Not included in Weekly Plan)",
                tickColor = R.color.gold_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "diamonds",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_super_like,
                highlight = "2",
                highlightColor = R.color.super_like_color,
                title = "Super Likes per Week",
                titleColor = R.color.white,
                tickColor = R.color.gold_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "super_likes",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_boost,
                highlight = "1",
                highlightColor = R.color.yellow,
                title = "Boost per Month",
                titleColor = R.color.white,
                subTitle = "(Not included in Weekly Plan)",
                tickColor = R.color.gold_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "boosts",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.gold_circle_badge,
                title = "Gold Badge",
                titleColor = R.color.white,
                tickColor = R.color.gold_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_no_ads,
                title = "No Ads",
                titleColor = R.color.white,
                tickColor = R.color.gold_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_eye,
                title = "See who likes you",
                titleColor = R.color.white,
                tickColor = R.color.white,
                tickIcon = R.drawable.ic_lock,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_location_filter,
                title = "Location Filter",
                titleColor = R.color.white,
                tickColor = R.color.white,
                tickIcon = R.drawable.ic_lock,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_ai_selected,
                title = "AI matchmaker",
                titleColor = R.color.white,
                tickColor = R.color.white,
                tickIcon = R.drawable.ic_lock,
            )
        )
        return list
    }

    fun getPlatinumFeatureList(): ArrayList<PlanFeatureModel> {
        val list = arrayListOf<PlanFeatureModel>()

        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_message_unselected,
                title = "Unlock Messaging",
                titleColor = R.color.white,
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_like_infi,
                title = "Unlimited Likes",
                titleColor = R.color.white,
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_rewinds_infi,
                title = "Unlimited Rewinds",
                titleColor = R.color.white,
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_diamond,
                highlight = "500",
                highlightColor = R.color.diamond_color,
                title = "Diamonds per Month",
                titleColor = R.color.white,
                subTitle = "(Not included in Weekly Plan)",
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "diamonds",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_super_like,
                highlight = "5",
                highlightColor = R.color.super_like_color,
                title = "Super Likes per Week",
                titleColor = R.color.white,
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "super_likes",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_boost,
                highlight = "2",
                highlightColor = R.color.yellow,
                title = "Boost per Month",
                titleColor = R.color.white,
                subTitle = "(Not included in Weekly Plan)",
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "boosts",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.platinum_circle_badge,
                title = "Platinum Badge",
                titleColor = R.color.white,
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_no_ads,
                title = "No Ads",
                titleColor = R.color.white,
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_eye,
                title = "See who likes you",
                titleColor = R.color.white,
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_location_filter,
                title = "Location Filter",
                titleColor = R.color.white,
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_ai_selected,
                title = "AI matchmaker",
                titleColor = R.color.white,
                tickColor = R.color.platinum_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        return list
    }

    fun getLifetimeFeatureList(): ArrayList<PlanFeatureModel> {
        val list = arrayListOf<PlanFeatureModel>()

        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_message_unselected,
                title = "Unlock Messaging",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_like_infi,
                title = "Unlimited Likes",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_rewinds_infi,
                title = "Unlimited Rewinds",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_diamond,
                highlight = "Infinite",
                highlightColor = R.color.diamond_color,
                title = "Diamonds",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "diamonds",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_super_like,
                highlight = "8",
                highlightColor = R.color.super_like_color,
                title = "Super Likes per Week",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "super_likes",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_boost,
                highlight = "4",
                highlightColor = R.color.yellow,
                title = "Boost per Month",
                titleColor = R.color.white,
                subTitle = "(Not included in Weekly Plan)",
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
                tag = "boosts",
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.lifetime_circle_badge,
                title = "Lifetime Badge",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_no_ads,
                title = "No Ads",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_eye,
                title = "See who likes you",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_location_filter,
                title = "Location Filter",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        list.add(
            PlanFeatureModel(
                icon = R.drawable.ic_ai_selected,
                title = "AI matchmaker",
                titleColor = R.color.white,
                tickColor = R.color.lifetime_plan,
                tickIcon = R.drawable.ic_tick,
            )
        )
        return list
    }
}