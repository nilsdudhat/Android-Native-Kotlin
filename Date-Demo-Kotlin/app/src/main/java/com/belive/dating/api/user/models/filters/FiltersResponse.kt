package com.belive.dating.api.user.models.filters

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FiltersResponse(
    @SerializedName("data")
    val filters: Filters,
    val message: String,
) : Parcelable

@Parcelize
data class Filters(
    @SerializedName("age_max")
    val ageMax: Int,
    @SerializedName("age_min")
    val ageMin: Int,
    val createdAt: String,
    @SerializedName("cust_latitude")
    val custLatitude: String,
    @SerializedName("cust_longitude")
    val custLongitude: String,
    val distance: Int,
    val id: Int,
    @SerializedName("is_ad_block")
    val isAdBlock: Int,
    @SerializedName("is_new_like")
    val isNewLike: Int,
    @SerializedName("is_new_message")
    val isNewMessage: Int,
    @SerializedName("is_show_age")
    val isShowAge: Int,
    @SerializedName("is_show_distance")
    val isShowDistance: Int,
    @SerializedName("is_show_seeing_interest")
    val isShowSeeingInterest: Int,
    @SerializedName("seeing_interest")
    val seeingInterest: Int,
    val status: Int,
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int
) : Parcelable