package com.belive.dating.api.user.models.profile_balances

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileBalanceResponse(
    @SerializedName("data")
    val profileBalance: ProfileBalance,
    val message: String,
) : Parcelable

@Parcelize
data class ProfileBalance(
    val boosts: Int,
    val diamonds: Int,
    val likes: Int,
    val rewinds: Int,
    @SerializedName("super_likes")
    val superLikes: Int
) : Parcelable