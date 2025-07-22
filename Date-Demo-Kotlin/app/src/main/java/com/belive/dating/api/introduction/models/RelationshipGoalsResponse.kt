package com.belive.dating.api.introduction.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class RelationshipGoalsResponse(
    @SerializedName("data")
    val `data`: List<RelationshipGoalData>,
    val message: String,
) : Parcelable

@Parcelize
data class RelationshipGoalData(
    @SerializedName("icons")
    val icon: String,
    var id: Int,
    val name: String,
) : Parcelable