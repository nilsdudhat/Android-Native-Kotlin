package com.belive.dating.api.user.models.diamond_history

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiamondHistoryResponse(
    @SerializedName("data")
    val diamondTransactionList: List<DiamondTransaction>,
    val message: String,
    val pagination: Pagination,
) : Parcelable

@Parcelize
data class DiamondTransaction(
    val amount: Int,
    val id: Int,
    @SerializedName("txn_date")
    val txnDate: String,
    @SerializedName("txn_type")
    val txnType: String,
) : Parcelable

@Parcelize
data class Pagination(
    @SerializedName("current_data")
    val currentData: Int,
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("data_count")
    val dataCount: Int,
    val limit: Int,
    @SerializedName("page_count")
    val pageCount: Int,
) : Parcelable