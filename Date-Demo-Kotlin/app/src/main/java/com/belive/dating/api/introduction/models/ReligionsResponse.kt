package com.belive.dating.api.introduction.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReligionsResponse(
    @SerializedName("data")
    val religionList: List<ReligionData>,
    val message: String,
) : Parcelable

@Parcelize
data class ReligionData(
    val id: Int,
    val name: String,
) : Parcelable