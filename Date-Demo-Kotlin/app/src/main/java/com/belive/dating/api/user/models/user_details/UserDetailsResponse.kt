package com.belive.dating.api.user.models.user_details

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserDetailsResponse(
    @SerializedName("data")
    val userDetails: UserDetails,
    val message: String,
) : Parcelable

@Parcelize
data class UserDetails(
    @SerializedName("about_me")
    val aboutMe: String?,
    val age: Int?,
    val school: String?,
    @SerializedName("known_language")
    val knownLanguage: String?,
    val city: String?,
    val country: String?,
    val state: String?,
    val distance: Double?,
    @SerializedName("fullname")
    val fullName: String?,
    val gender: String?,
    val id: Int,
    val interests: List<Interest>?,
    @SerializedName("is_managed_by_admin")
    val isFake: Boolean?,
    @SerializedName("is_like")
    val isLike: Int?,
    @SerializedName("like_count")
    val likeCount: Int?,
    @SerializedName("lookingfor")
    val relationshipGoal: String?,
    @SerializedName("seeing_interest")
    val oppositeGender: String?,
    val height: Height?,
    @SerializedName("match_per")
    val matchPer: String?,
    val orientation: String?,
    @SerializedName("superlike_count")
    val superLikeCount: Int?,
    @SerializedName("userimages")
    val userImages: List<String>?,
    @SerializedName("zodiac_data")
    val zodiac: Basic?,
    @SerializedName("education_data")
    val education: Basic?,
    @SerializedName("religion_data")
    val religion: Basic?,
    @SerializedName("marital_status_data")
    val maritalStatus: Basic?,
    @SerializedName("family_plan_data")
    val familyPlan: Basic?,
    @SerializedName("personality_type")
    val personalityType: String?,
    @SerializedName("communication_type")
    val communicationType: String?,
    @SerializedName("love_type")
    val loveType: String?,
    @SerializedName("pets")
    val pet: String?,
    @SerializedName("drinking")
    val drinking: String?,
    @SerializedName("smoking")
    val smoking: String?,
    @SerializedName("workout")
    val workout: String?,
    @SerializedName("preffered_diet")
    val preferredDiet: String?,
    @SerializedName("social_media_status")
    val socialStatus: String?,
    @SerializedName("sleeping_habbit")
    val sleepHabit: String?,
) : Parcelable

@Parcelize
data class Basic(
    val id: Int,
    val name: String,
    val icon: String?,
) : Parcelable

@Parcelize
data class Height(
    val feet: Int,
    val inch: Int,
) : Parcelable

@Parcelize
data class Interest(
    val name: String,
    @SerializedName("icons")
    val icon: String,
) : Parcelable