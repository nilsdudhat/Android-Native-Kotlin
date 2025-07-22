package com.belive.dating.payment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class PlanFeatureModel(
    val icon: Int? = null,
    var highlight: String? = null,
    val highlightColor: Int? = null,
    val upgrade: String? = null,
    val upgradeColor: Int? = null,
    var title: String? = null,
    val titleColor: Int? = null,
    val subTitle: String? = null,
    var tickIcon: Int? = null,
    var tickColor: Int? = null,
    val tag: String? = null,
) : Parcelable