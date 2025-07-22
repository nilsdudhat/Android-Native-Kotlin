package com.belive.dating.api.user.models.like_profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LikeProfileResponse(
    @SerializedName("data")
    val likeProfile: LikeProfile?,
    val message: String,
) : Parcelable

@Parcelize
data class LikeProfile(
    val deductFrom: String,
    @SerializedName("is_like")
    val isLike: Boolean,
    @SerializedName("like_type")
    val likeType: Int,
    val totalLike: Int,
    @SerializedName("totalSuperlike")
    val totalSuperLike: Int,
) : Parcelable