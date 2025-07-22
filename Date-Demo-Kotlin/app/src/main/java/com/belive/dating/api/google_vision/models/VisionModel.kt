package com.belive.dating.api.google_vision.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CombinedRequest(
    @SerializedName("requests")
    val requests: List<Request>
) : Parcelable

@Parcelize
data class Request(
    @SerializedName("image")
    val image: Image,
    @SerializedName("features")
    val features: List<Feature>
) : Parcelable

@Parcelize
data class Image(
    @SerializedName("content")
    val content: String // Base64-encoded image string
) : Parcelable

@Parcelize
data class Feature(
    @SerializedName("type")
    val type: String, // Detection type
    @SerializedName("maxResults")
    val maxResults: Int = 10 // Optional: Adjust this as needed
) : Parcelable