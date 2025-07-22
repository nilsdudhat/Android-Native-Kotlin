package com.belive.dating.api.user.models.diamond_question

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiamondQuestionResponse(
    @SerializedName("data")
    val questionData: QuestionData,
    val message: String,
) : Parcelable

@Parcelize
data class QuestionData(
    val createdAt: String,
    @SerializedName("daily_checkin_reward")
    val dailyCheckInReward: Map<String, Int>,
    val description: String,
    val diamonds: Int,
    val icon: String?,
    val id: Int,
    @SerializedName("questions")
    val questionList: List<Question>,
    val status: Int,
    val title: String,
    val updatedAt: String,
) : Parcelable

@Parcelize
data class Question(
    @SerializedName("description")
    val descriptionList: List<Description>,
    val id: Int,
    val title: String,
) : Parcelable

@Parcelize
data class Description(
    @SerializedName("btn_txt")
    val btnTxt: String,
    val diamond: Int,
    val icon: Boolean,
    @SerializedName("is_claimed")
    val isClaimed: Boolean,
    @SerializedName("is_progress")
    val isProgress: Boolean,
    val status: Int,
    @SerializedName("sub_id")
    val subId: Int,
    val title: String,
) : Parcelable