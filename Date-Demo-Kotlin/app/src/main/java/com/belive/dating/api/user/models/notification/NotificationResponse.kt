package com.belive.dating.api.user.models.notification

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationResponse(
    @SerializedName("data")
    var data: ArrayList<NotificationModel> = arrayListOf(),
    @SerializedName("message")
    var message: String? = null,
) : Parcelable

@Parcelize
data class NotificationModel(
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("body")
    var body: String? = null,
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("createdAt")
    var createdAt: String? = null,
    @SerializedName("profile_data")
    var profileData: ProfileData? = ProfileData(),
) : Parcelable

@Parcelize
data class ProfileData(
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("fullname")
    var fullName: String? = null,
    @SerializedName("userimages")
    var userImages: ArrayList<UserImage> = arrayListOf(),
) : Parcelable

@Parcelize
data class UserImage(
    @SerializedName("image")
    var image: String? = null,
) : Parcelable