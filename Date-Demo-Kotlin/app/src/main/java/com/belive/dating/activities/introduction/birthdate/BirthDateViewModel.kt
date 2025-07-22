package com.belive.dating.activities.introduction.birthdate

import androidx.databinding.ObservableField
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class BirthDateViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val TAG = "BIRTH_DATE_VIEW_MODEL"

    val day = ObservableField("")
    val month = ObservableField("")
    val year = ObservableField("")
    val validationError = ObservableField("")
    val isNextEnabled = ObservableField(false)

    fun updateState() {
        savedStateHandle["${TAG}_day"] = day.get()
        savedStateHandle["${TAG}_month"] = month.get()
        savedStateHandle["${TAG}_year"] = year.get()
        savedStateHandle["${TAG}_validationError"] = validationError.get()
        savedStateHandle["${TAG}_isNextEnabled"] = isNextEnabled.get()
    }

    fun getState() {
        day.set(savedStateHandle["${TAG}_day"])
        month.set(savedStateHandle["${TAG}_month"])
        year.set(savedStateHandle["${TAG}_year"])
        validationError.set(savedStateHandle["${TAG}_validationError"])
        isNextEnabled.set(savedStateHandle["${TAG}_isNextEnabled"])
    }
}