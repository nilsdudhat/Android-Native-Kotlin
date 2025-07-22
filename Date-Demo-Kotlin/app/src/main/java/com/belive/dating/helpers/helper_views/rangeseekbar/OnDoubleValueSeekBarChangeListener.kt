package com.belive.dating.helpers.helper_views.rangeseekbar

interface OnDoubleValueSeekBarChangeListener {
    fun onValueChanged(seekBar: DoubleValueSeekBarView?, min: Int, max: Int, fromUser: Boolean)
    fun onStartTrackingTouch(seekBar: DoubleValueSeekBarView?, min: Int, max: Int)
    fun onStopTrackingTouch(seekBar: DoubleValueSeekBarView?, min: Int, max: Int)
}