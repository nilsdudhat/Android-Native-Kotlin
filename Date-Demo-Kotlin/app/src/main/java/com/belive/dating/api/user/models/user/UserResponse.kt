package com.belive.dating.api.user.models.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserResponse(
    @SerializedName("data")
    val user: User,
    val message: String,
) : Parcelable

@Parcelize
data class User(
    @SerializedName("about_me")
    val aboutMe: String?,
    val age: Int,
    @SerializedName("ai_remain_profile_count")
    val aiRemainProfileCount: Int,
    @SerializedName("birth_date")
    val birthDate: String,
    val city: String,
    val completeProfilePer: Int,
    val country: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("current_app_version")
    val currentAppVersion: String,
    @SerializedName("current_date")
    val currentDate: Long,
    @SerializedName("dont_show_my_age")
    val hideAge: Boolean,
    @SerializedName("dont_show_my_distance")
    val hideDistance: Boolean,
    @SerializedName("cust_latitude")
    val customLatitude: String,
    @SerializedName("cust_longitude")
    val customLongitude: String,
    @SerializedName("school")
    val school: String?,
    @SerializedName("known_language")
    val knownLanguage: String?,
    val height: Height?,
    val email: String,
    @SerializedName("fullname")
    val fullName: String,
    val gender: String,
    val id: Int,
    @SerializedName("is_notification")
    val isNotification: Int,
    val latitude: String,
    val likeCount: Int,
    @SerializedName("login_type")
    val loginType: Int,
    val longitude: String,
    @SerializedName("lookingfor")
    val relationshipGoal: RelationshipGoal?,
    @SerializedName("my_interests")
    val myInterests: String?,
    val orientation: Orientation?,
    val purchase: Purchase?,
    val referReward: Int,
    @SerializedName("selfie")
    val selfie: UserImage?,
    @SerializedName("seeing_interest")
    val oppositeGender: String?,
    val state: String,
    val superLikeCount: Int,
    val type: String,
    val unreadNotification: Int,
    @SerializedName("userimages")
    val userImages: List<UserImage>,
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
data class RelationshipGoal(
    @SerializedName("icons")
    val icon: String,
    val id: Int,
    val name: String,
) : Parcelable

@Parcelize
data class Height(
    val feet: Int,
    val inch: Int,
) : Parcelable

@Parcelize
data class Orientation(
    val id: Int,
    val name: String,
) : Parcelable

@Parcelize
data class UserImage(
    val image: String,
    val position: Int,
    val verification: String,
) : Parcelable

@Parcelize
data class Basic(
    val id: Int,
    val name: String,
    val icon: String?,
) : Parcelable

@Parcelize
data class Purchase(
    @SerializedName("package")
    val activePlan: String,
    @SerializedName("product_id")
    val productID: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("unlimited_likes")
    val unlimitedLikes: Boolean,
    @SerializedName("unlimited_rewinds")
    val unlimitedRewinds: Boolean,
    @SerializedName("lifetime_badge")
    val lifetimeBadge: Boolean,
    @SerializedName("platinum_badge")
    val platinumBadge: Boolean,
    @SerializedName("gold_badge")
    val goldBadge: Boolean,
    @SerializedName("see_who_likes_you")
    val seeWhoLikesYou: Boolean,
    @SerializedName("location_filter")
    val locationFilter: Boolean,
    @SerializedName("ai_matchmaker")
    val aiMatchmaker: Boolean,
    @SerializedName("no_ads")
    val noAds: Boolean,
) : Parcelable
