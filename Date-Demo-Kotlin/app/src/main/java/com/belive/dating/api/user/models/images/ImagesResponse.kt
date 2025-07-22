package com.belive.dating.api.user.models.images

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImagesResponse(
    @SerializedName("data")
    val imageList: List<ImageInfo>,
    val message: String,
) : Parcelable

@Parcelize
data class ImageInfo(
    val image: String,
    val verification: String,
    val position: Int,
) : Parcelable