package com.udemy.kotlin.market.app.interfaces

import android.view.View

interface ItemClickListener {
    fun onClick(view: View, position: Int)
}