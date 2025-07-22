package com.belive.dating.activities.introduction.gender

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class GenderViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "GENDER_VIEW_MODEL"

    val selectedIndex = ObservableField(0)
    val isNextEnabled = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_selectedIndex"] = selectedIndex.get()
        savedStateHandle["${TAG}_isNextEnabled"] = isNextEnabled.get()
    }

    fun getState() {
        selectedIndex.set(savedStateHandle["${TAG}_selectedIndex"])
        isNextEnabled.set(savedStateHandle["${TAG}_isNextEnabled"])
    }
}