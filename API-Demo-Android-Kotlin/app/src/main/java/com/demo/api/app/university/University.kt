package com.demo.api.app.university

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class University(
    val alpha_two_code: String?,
    val country: String?,
    val domains: List<String>?,
    val name: String?,
    @SerializedName("state-province")
    val state_province: String?,
    val web_pages: List<String>?,
) : Parcelable, Serializable