package com.belive.dating.api.user.models.basic

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BasicResponse(
    @SerializedName("data")
    val basicData: BasicData,
    val message: String,
) : Parcelable

@Parcelize
data class BasicData(
    val id: Int,
    val category: String,
    @SerializedName("basicData")
    val basics: List<Basic>,
) : Parcelable

@Parcelize
data class Basic(
    val id: Int,
    @SerializedName("category_id")
    val categoryId: Int,
    val basic: String,
    val icons: String,
) : Parcelable