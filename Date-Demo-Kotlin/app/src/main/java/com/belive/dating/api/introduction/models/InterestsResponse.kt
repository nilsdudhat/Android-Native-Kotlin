package com.belive.dating.api.introduction.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class InterestsResponse(
    @SerializedName("data") val `data`: ArrayList<InterestData>,
    val message: String
) : Parcelable

@Parcelize
data class InterestData(
    @SerializedName("icons")
    val icon: String,
    var id: Int,
    val name: String,
    var isChecked: Boolean? = null,
) : Parcelable