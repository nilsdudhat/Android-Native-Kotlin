package com.belive.dating.activities.settings.content_visibility

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class ContentVisibilityViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() {

    val showAboutMe = ObservableField(true)
    val showZodiac = ObservableField(true)
    val showEducation = ObservableField(true)
    val showOppositeGender = ObservableField(true)
    val showSexualOrientation = ObservableField(true)
    val showInterest = ObservableField(true)

    val isButtonEnabled = ObservableField(false)
}