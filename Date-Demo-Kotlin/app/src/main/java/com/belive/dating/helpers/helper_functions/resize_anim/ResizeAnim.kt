package com.belive.dating.helpers.helper_functions.resize_anim

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import com.belive.dating.extensions.logger

class ResizeAnim(var view: View, private val startHeight: Int, private val targetHeight: Int) : Animation() {

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        if (startHeight == 0 || targetHeight == 0) {
            view.layoutParams.height =
                (startHeight + (targetHeight - startHeight) * interpolatedTime).toInt()
        } else {
            view.layoutParams.height = (startHeight + targetHeight * interpolatedTime).toInt()

            logger("--resize--", "applyTransformation: height = ${view.layoutParams.height}")
        }
        view.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}
