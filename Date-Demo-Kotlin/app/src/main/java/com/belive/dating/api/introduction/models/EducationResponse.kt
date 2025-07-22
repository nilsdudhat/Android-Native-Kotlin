package com.belive.dating.api.introduction.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class EducationResponse(
    @SerializedName("data")
    val educationList: List<EducationData>,
    val message: String,
) : Parcelable

@Parcelize
data class EducationData(
    val id: Int,
    val name: String,
) : Parcelable