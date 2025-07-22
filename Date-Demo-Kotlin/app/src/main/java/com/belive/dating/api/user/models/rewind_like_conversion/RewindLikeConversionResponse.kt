package com.belive.dating.api.user.models.rewind_like_conversion

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RewindLikeConversionResponse(
    @SerializedName("data")
    val rewindLikeModel: RewindLikeModel,
    val message: String,
) : Parcelable

@Parcelize
data class RewindLikeModel(
    val id: Int,
    var balance: Int,
    @SerializedName("item_flag")
    val itemFlag: String,
    @SerializedName("purchase_item")
    val purchaseItem: Int,
    @SerializedName("spended_diamond")
    val spendedDiamond: Int,
) : Parcelable