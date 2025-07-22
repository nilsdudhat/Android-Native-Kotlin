package com.belive.dating.api.introduction.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class SexualOrientationResponse(
    @SerializedName("data")
    val sexualOrientationList: List<Orientation>?,
    val message: String?,
) : Parcelable

@Parcelize
data class Orientation(
    val id: Int,
    val name: String,
) : Parcelable