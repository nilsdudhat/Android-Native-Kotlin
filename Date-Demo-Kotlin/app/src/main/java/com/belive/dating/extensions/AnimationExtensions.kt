package com.belive.dating.extensions

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.app.Activity.OVERRIDE_TRANSITION_OPEN
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.ColorUtils
import com.belive.dating.R

fun View.setBackgroundAnimation(colorArray: IntArray) {
    val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorArray)
    background = gradientDrawable

    post {
        gradientDrawable.setBounds(-2 * width, 0, width, height)

        val animator = ValueAnimator.ofInt(0, 2 * width).apply {
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            duration = 1500

            addUpdateListener { progressAnimator ->
                val progress = (progressAnimator.animatedValue as Int)
                gradientDrawable.setBounds(-2 * width + progress, 0, width + progress, height)
            }
        }

        // Color transition animator
        val colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            duration = 2000

            addUpdateListener { valueAnimator ->
                val fraction = valueAnimator.animatedFraction
                val blendedColors = IntArray(colorArray.size - 1) { i ->
                    blendColors(colorArray[i], colorArray[i + 1], fraction)
                }
                gradientDrawable.colors = blendedColors
            }
        }

        animator.start()
        colorAnimator.start()
    }
}

private fun blendColors(color1: Int, color2: Int, fraction: Float): Int {
    return ColorUtils.blendARGB(color1, color2, fraction)
}

fun Activity.noAnimation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
    } else {
        overridePendingTransition(0, 0)
    }
}

fun Activity.swipeLeft() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.slide_in_left, R.anim.slide_out_right)
    } else {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}

fun Activity.swipeRight() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left)
    } else {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}

fun Activity.swipeUp() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_up, R.anim.no_animation)
    } else {
        overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
    }
}

fun Activity.swipeDown() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.no_animation, R.anim.slide_down)
    } else {
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
    }
}

fun Activity.fadeIn() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.fade_in, R.anim.fade_out)
    } else {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}