package com.belive.dating.activities.introduction.upload_photo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class PhotoValidationModel(
    val path: String?,
    var reject: Reject? = null,
    var isNetworkImage: Boolean = false,
    val reason: String? = null,
) : Parcelable

enum class Reject {
    // compression validations
    TOO_LOW_RESOLUTION,

    // processing error
    SOMETHING_WRONG,

    // face validations
    FACE_NOT_FOUND,

    // safe search validations
    NOT_SAFE,

    // blur validations
    BLURRED,

    // admin has rejected the photo
    ADMIN_REJECTED,
}