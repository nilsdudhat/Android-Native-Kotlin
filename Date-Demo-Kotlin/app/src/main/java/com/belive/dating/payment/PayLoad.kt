package com.belive.dating.payment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class PayLoad(
    var packageName: String? = null,
    var productId: String? = null,
    var purchaseToken: String? = null,
) : Parcelable