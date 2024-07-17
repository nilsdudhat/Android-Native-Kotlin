package com.udemy.journal.app.models

import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Journal(
    @Bindable
    var userID: String = "",
    @Bindable
    var docID: String = "",
    @Bindable
    var title: String = "",
    @Bindable
    var imageUrl: String = "",
    @Bindable
    var thoughts: String = "",
    @Bindable
    var timeAdded: Timestamp = Timestamp.now(),
) : Parcelable, BaseObservable()