package com.udemy.databinding.app

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class Vehicle: BaseObservable() {
    var modelYear: String = ""

    /*
    * @Bindable : used to mark properties for which the data binding library should generate a BR class,
    * which triggers automatic updates in the UI that is bound to that property.
    * */
    @Bindable
    var name: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }
}