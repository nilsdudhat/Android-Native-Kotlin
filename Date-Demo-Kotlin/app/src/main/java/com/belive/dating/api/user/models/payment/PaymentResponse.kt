package com.belive.dating.api.user.models.payment

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentResponse(
    @SerializedName("data")
    val payment: PaymentData,
    val message: String,
) : Parcelable

@Parcelize
data class PaymentData(
    @SerializedName("purchase")
    val purchaseModel: PaymentModel,
    @SerializedName("balance")
    val profileBalanceModel: ProfileBalanceModel,
) : Parcelable

@Parcelize
data class PaymentModel(
    @SerializedName("is_topup")
    val isTopUp: Int,
    val price: Int,
    @SerializedName("package")
    val planName: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("unlimited_likes")
    val unlimitedLikes: Boolean = false,
    @SerializedName("unlimited_rewinds")
    val unlimitedRewinds: Boolean = false,
    @SerializedName("lifetime_badge")
    val lifetimeBadge: Boolean = false,
    @SerializedName("platinum_badge")
    val platinumBadge: Boolean = false,
    @SerializedName("gold_badge")
    val goldBadge: Boolean = false,
    @SerializedName("see_who_likes_you")
    val seeWhoLikesYou: Boolean = false,
    @SerializedName("location_filter")
    val locationFilter: Boolean = false,
    @SerializedName("ai_matchmaker")
    val aiMatchmaker: Boolean = false,
    @SerializedName("no_ads")
    val noAds: Boolean = false,
) : Parcelable

@Parcelize
data class ProfileBalanceModel(
    @SerializedName("diamonds")
    val totalDiamonds: Int,
    @SerializedName("super_likes")
    val totalSuperLikes: Int,
    @SerializedName("boosts")
    val totalBoosts: Int,
    @SerializedName("rewinds")
    val totalRewinds: Int,
    @SerializedName("likes")
    val totalLikes: Int,
) : Parcelable