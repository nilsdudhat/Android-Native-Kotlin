package com.belive.dating.api.user.models.home_profiles

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeUsersResponse(
    @SerializedName("data")
    val userList: ArrayList<User>,
    val message: String,
) : Parcelable

@Parcelize
data class User(
    val age: Int?,
    val city: String?,
    val country: String?,
    val distance: Double?,
    @SerializedName("fullname")
    val fullName: String?,
    val gender: String?,
    val id: Int,
    val interests: List<Interest>?,
    @SerializedName("is_managed_by_admin")
    val isFake: Boolean?,
    @SerializedName("like_count")
    val likeCount: Int?,
    @SerializedName("match_per")
    val matchPercentage: String?,
    val state: String?,
    @SerializedName("userimages")
    val userImages: List<UserImages>?,
    var deductFrom: String?,
) : Parcelable

@Parcelize
data class UserImages(
    val id: Int,
    val image: String,
    val verification: String,
) : Parcelable

@Parcelize
data class Interest(
    val name: String,
) : Parcelable