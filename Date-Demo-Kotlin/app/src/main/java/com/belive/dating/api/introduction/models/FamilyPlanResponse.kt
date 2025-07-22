package com.belive.dating.api.introduction.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FamilyPlanResponse(
    @SerializedName("data")
    val familyPlanList: List<FamilyPlanData>,
    val message: String,
) : Parcelable

@Parcelize
data class FamilyPlanData(
    val id: Int,
    val name: String,
) : Parcelable