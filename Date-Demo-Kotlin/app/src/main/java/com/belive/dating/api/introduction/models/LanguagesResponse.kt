package com.belive.dating.api.introduction.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LanguagesResponse(
    @SerializedName("data")
    val languageList: List<LanguageData>,
    val message: String,
) : Parcelable

@Parcelize
data class LanguageData(
    val id: Int,
    val name: String,
    var isChecked: Boolean? = null,
) : Parcelable