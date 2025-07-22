package com.belive.dating.api.user.models.edit_images

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class EditImagesResponse(
    @SerializedName("data")
    val userModel: UserModel,
    val message: String,
) : Parcelable

@Parcelize
data class UserModel(
    @SerializedName("images")
    val editedImageList: List<EditedImage>,
    val completeProfilePer: Int,
) : Parcelable

@Parcelize
data class EditedImage(
    @SerializedName("image")
    val editedImage: String,
) : Parcelable