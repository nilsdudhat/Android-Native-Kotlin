package com.belive.dating.api.user.models.add_location

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddLocationModel(
    @SerializedName("data")
    val location: MyLocation,
    val message: String,
) : Parcelable

@Parcelize
data class MyLocation(
    val createdAt: String,
    val id: Int,
    val latitude: String,
    val longitude: String,
    val name: String,
    val status: Int,
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int,
) : Parcelable