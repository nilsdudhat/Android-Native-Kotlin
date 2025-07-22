package com.belive.dating.api.user.models.liked_me_profiles

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LikedMeProfilesResponse(
    @SerializedName("data")
    val likedMeProfileList: ArrayList<LikedMeProfile>,
    val message: String,
    val pagination: Pagination,
) : Parcelable

@Parcelize
data class LikedMeProfile(
    @SerializedName("data")
    val aboutMe: String,
    val distance: Double,
    val age: Int,
    val education: String,
    @SerializedName("fullname")
    val fullName: String,
    val gender: String,
    val id: Int,
    val interests: List<String>,
    @SerializedName("like_count")
    val likeCount: Int,
    @SerializedName("lookingfor")
    val relationshipGoal: String,
    @SerializedName("match_per")
    val matchPer: String,
    val orientation: String,
    @SerializedName("superlike_count")
    val superLikeCount: Int,
    @SerializedName("userimages")
    val userImages: List<String>,
    val zodiac: String,
) : Parcelable

@Parcelize
data class Pagination(
    @SerializedName("current_data")
    val currentData: Int,
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("data_count")
    val dataCount: Int,
    val limit: Int,
    @SerializedName("page_count")
    val pageCount: Int,
) : Parcelable