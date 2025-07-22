package com.belive.dating.api.user.models.home

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RewindDeductResponse(
    @SerializedName("data")
    val rewind: RewindModel,
    val message: String,
) : Parcelable

@Parcelize
data class RewindModel(
    @SerializedName("is_rewind")
    val isRewind: Boolean,
    val totalRewind: Int,
    val totalLike: Int,
    @SerializedName("totalSuperlike")
    val totalSuperLike: Int,
) : Parcelable