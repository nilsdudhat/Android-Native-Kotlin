package com.belive.dating.api.user.models.search_user

import com.google.gson.annotations.SerializedName

data class SearchUserResponse(
    @SerializedName("data")
    val userList: List<User>,
    val message: String,
)

data class User(
    @SerializedName("fullname")
    val fullName: String,
    val id: Int,
    val interests: List<Interest>,
    @SerializedName("is_like")
    val isLike: Int,
    @SerializedName("like_count")
    val likeCount: Int,
    @SerializedName("lookingfor")
    val relationshipGoal: String,
    @SerializedName("orientation")
    val orientation: String,
    @SerializedName("superlike_count")
    val superLikeCount: Int,
    @SerializedName("userimages")
    val userImages: List<String>,
)

data class Interest(
    val icons: String,
    val name: String,
)