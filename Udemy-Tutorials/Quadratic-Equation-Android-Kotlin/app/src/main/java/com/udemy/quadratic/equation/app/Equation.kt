package com.udemy.quadratic.equation.app

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.util.Locale
import kotlin.math.sqrt

class Equation : BaseObservable() {
    var a: String = ""
    var b: String = ""
    var c: String = ""

    @Bindable
    var rootX: String = ""

    @Bindable
    var rootY: String = ""

    fun calculate(view: View) {
        val discriminant = b.toDouble() * b.toDouble() - 4 * a.toDouble() * c.toDouble()

        if (discriminant < 0) {
            rootX = "no real root available"
            rootY = "no real root available"
        } else {
            val tempX = (-b.toDouble() + sqrt(discriminant)) / (2 * a.toDouble())
            val tempY = (-b.toDouble() - sqrt(discriminant)) / (2 * a.toDouble())

            rootX = String.format(Locale.getDefault(), "%.2f", tempX)
            rootY = String.format(Locale.getDefault(), "%.2f", tempY)
        }

        notifyPropertyChanged(BR.rootX)
        notifyPropertyChanged(BR.rootY)
    }
}