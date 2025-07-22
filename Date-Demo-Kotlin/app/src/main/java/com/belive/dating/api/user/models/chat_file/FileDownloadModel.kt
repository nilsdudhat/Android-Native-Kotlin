package com.belive.dating.api.user.models.chat_file

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileDownloadModel(
    val file: String? = null,
    var progress: Int = 0,
    var userId: Int,
    var friendId: Int,
    var msgId: Int,
    @SerializedName("media_type")
    var mediaType: Int,
    @SerializedName("media_name")
    var mediaName: String?,
) : Parcelable

@Parcelize
data class FileUploadModel(
    val file: String? = null,
    var progress: Int = 0,
    var userId: Int,
    var friendId: Int,
    var msgId: Int,
    @SerializedName("media_type")
    var mediaType: Int,
    var sending: Int = 0,
) : Parcelable