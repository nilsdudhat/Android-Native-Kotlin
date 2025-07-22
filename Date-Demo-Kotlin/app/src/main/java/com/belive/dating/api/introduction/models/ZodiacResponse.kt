package com.belive.dating.api.introduction.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ZodiacResponse(
    @SerializedName("data")
    val zodiacList: List<ZodiacData>,
    val message: String,
) : Parcelable

@Parcelize
data class ZodiacData(
    val id: Int,
    val name: String,
    val icon: String,
) : Parcelable