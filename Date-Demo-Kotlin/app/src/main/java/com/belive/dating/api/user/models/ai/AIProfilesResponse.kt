package com.belive.dating.api.user.models.ai

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AIProfilesResponse(
    @SerializedName("data")
    val aiProfileList: ArrayList<AIProfile>,
    val message: String,
    val pagination: Pagination,
) : Parcelable

@Parcelize
data class AIProfile(
    @SerializedName("about_me")
    val aboutMe: String,
    val age: Int,
    val city: String,
    val country: String,
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
    val state: String,
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