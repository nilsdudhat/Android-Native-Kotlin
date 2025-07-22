package com.belive.dating.api.introduction.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaritalStatusResponse(
    @SerializedName("data")
    val maritalStatusList: List<MaritalStatusData>,
    val message: String,
) : Parcelable

@Parcelize
data class MaritalStatusData(
    val id: Int,
    val name: String,
) : Parcelable