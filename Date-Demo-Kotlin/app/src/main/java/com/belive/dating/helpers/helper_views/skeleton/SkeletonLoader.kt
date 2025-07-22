package com.belive.dating.helpers.helper_views.skeleton

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R

fun ViewGroup.showSkeleton(isSkeleton: Boolean) {
    blockTouches(isSkeleton)

    // First, apply the skeleton effect to the ViewGroup itself
    if (isSkeleton) {
        if ((getTag(R.string.skeleton_tag) == null) || (getTag(R.string.skeleton_tag) as? Boolean == false)) {
            setTag(R.string.skeleton_tag, true)
            saveOriginalPadding()
            setTag(R.string.background_tag, background) // Save the original background
            background = null
            applySkeletonEffectToView()
        }
    } else {
        if (getTag(R.string.skeleton_tag) as? Boolean == true) {
            setTag(R.string.skeleton_tag, false)
            stopSkeletonAnimation()
            background = getTag(R.string.background_tag) as? Drawable // Restore the original background
            setTag(R.string.background_tag, null)
            clearAnimation()
            restoreOriginalPadding()
            invalidate()
        }
    }

    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is RecyclerView) {

        } else if (child is ViewGroup) {
            child.showSkeleton(isSkeleton)
        } else {
            if (isSkeleton) {
                if ((child.getTag(R.string.skeleton_tag) == null) || (child.getTag(R.string.skeleton_tag) as? Boolean == false)) {
                    child.setTag(R.string.skeleton_tag, true)
                    child.saveOriginalPadding()
                    child.setTag(R.string.background_tag, child.background) // Save the original background
                    child.applySkeletonEffectToView()
                }
            } else {
                if (child.getTag(R.string.skeleton_tag) as? Boolean == true) {
                    child.setTag(R.string.skeleton_tag, false)
                    child.stopSkeletonAnimation()
                    child.background = child.getTag(R.string.background_tag) as? Drawable // Restore the original background
                    child.setTag(R.string.background_tag, null)
                    child.clearAnimation()
                    child.restoreOriginalPadding()
                    if (child is ImageView) {
                        child.imageTintList = child.getTag(R.string.image_tint_tag) as? ColorStateList
                        child.setTag(R.string.image_tint_tag, null)
                    } else if (child is TextView) {
                        (child.getTag(R.string.text_color_tag) as? Int)?.let { child.setTextColor(it) }
                        child.setTag(R.string.text_color_tag, null)
                    }
                    child.invalidate()
                }
            }
        }
    }
}

fun View.showSkeleton(isSkeleton: Boolean) {
    blockTouches(isSkeleton)

    if (this is RecyclerView) {

    } else if (this is ViewGroup) {
        showSkeleton(isSkeleton)
    } else {
        if (isSkeleton) {
            if ((getTag(R.string.skeleton_tag) == null) || (getTag(R.string.skeleton_tag) as? Boolean == false)) {
                setTag(R.string.skeleton_tag, true)
                saveOriginalPadding()
                setTag(R.string.background_tag, background) // Save the original background
                applySkeletonEffectToView()
            }
        } else {
            if (getTag(R.string.skeleton_tag) as? Boolean == true) {
                setTag(R.string.skeleton_tag, false)
                stopSkeletonAnimation()
                background = getTag(R.string.background_tag) as? Drawable // Restore the original background
                setTag(R.string.background_tag, null)
                clearAnimation()
                restoreOriginalPadding()
                if (this is ImageView) {
                    imageTintList = getTag(R.string.image_tint_tag) as? ColorStateList
                    setTag(R.string.image_tint_tag, null)
                } else if (this is TextView) {
                    (getTag(R.string.text_color_tag) as? Int)?.let { setTextColor(it) }
                    setTag(R.string.text_color_tag, null)
                }
                invalidate()
            }
        }
    }
}

fun View.blockTouches(isSkeleton: Boolean) {
    if (isSkeleton) {
        setOnTouchListener { _, _ -> true }
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.blockTouches(true)
            }
        }
    } else {
        setOnTouchListener(null) // Removes the touch listener
        if (this is ViewGroup) {
            for (i in 0 until this.childCount) {
                val child = getChildAt(i)
                child.blockTouches(false)
            }
        }
    }
}

fun View.clearTextFromSkeleton() {
    when (this) {
        is RecyclerView -> {

        }

        is ViewGroup -> {
            for (i in 0 until childCount) {
                getChildAt(i).clearTextFromSkeleton()
            }
        }

        is TextView -> {
            setTag(R.string.text_color_tag, currentTextColor)
            setTextColor(Color.TRANSPARENT)
        }

        is ImageView -> {
            setTag(R.string.image_tint_tag, imageTintList)
            imageTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        }
    }
}

fun View.applySkeletonEffectToView() {
    clearTextFromSkeleton()

    post {
        val paint = Paint().apply {
            shader = LinearGradient(
                -width.toFloat(), 0f, 0f, height.toFloat(), intArrayOf(
                    ContextCompat.getColor(context, R.color.primary_color_05),
                    ContextCompat.getColor(context, R.color.primary_color_10), // Highlight
                    ContextCompat.getColor(context, R.color.primary_color_05),
                ), floatArrayOf(0.2f, 0.5f, 0.8f), // Gradient positions for smoother transitions
                Shader.TileMode.CLAMP
            )
        }

        val shapeDrawable = object : ShapeDrawable() {
            override fun draw(canvas: Canvas) {
                canvas.drawRect(bounds, paint)
            }
        }

        background = shapeDrawable

        // Create an animator for shimmer effect
        val shimmerAnimator = ValueAnimator.ofFloat(-width.toFloat(), width.toFloat() * 2).apply {
            duration = 1500L // Adjust speed of shimmer
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator() // Smooth acceleration and deceleration

            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float

                // Update the gradient shader dynamically
                paint.shader = LinearGradient(
                    animatedValue, 0f, animatedValue + width, height.toFloat(), intArrayOf(
                        ContextCompat.getColor(context, R.color.primary_color_05),
                        ContextCompat.getColor(context, R.color.primary_color_10), // Highlight
                        ContextCompat.getColor(context, R.color.primary_color_05),
                    ), floatArrayOf(0.2f, 0.5f, 0.8f), Shader.TileMode.CLAMP
                )

                // Redraw the ShapeDrawable
                invalidate()
            }
        }

        shimmerAnimator.start()

        val pulseAnimatorX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.05f, 1f).apply {
            duration = 1000L
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val pulseAnimatorY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.05f, 1f).apply {
            duration = 1000L
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        // Combine animations
        val animatorSet = AnimatorSet().apply {
            playTogether(shimmerAnimator, pulseAnimatorX, pulseAnimatorY)
            start()
        }

        // Save the animator set in the tag to stop later
        setTag(R.string.animator_tag, animatorSet)
    }
}

fun ViewGroup.applySkeletonEffectToView() {
    post {
        val paint = Paint().apply {
            shader = LinearGradient(
                -width.toFloat(), 0f, 0f, height.toFloat(), intArrayOf(
                    ContextCompat.getColor(context, R.color.primary_color_05),
                    ContextCompat.getColor(context, R.color.primary_color_10), // Highlight
                    ContextCompat.getColor(context, R.color.primary_color_05),
                ), floatArrayOf(0.2f, 0.5f, 0.8f), // Gradient positions for smoother transitions
                Shader.TileMode.CLAMP
            )
        }

        val shapeDrawable = object : ShapeDrawable() {
            override fun draw(canvas: Canvas) {
                canvas.drawRect(bounds, paint)
            }
        }

        background = shapeDrawable

        // Create an animator for shimmer effect
        val shimmerAnimator = ValueAnimator.ofFloat(-width.toFloat(), width.toFloat() * 2).apply {
            duration = 1500L // Adjust speed of shimmer
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator() // Smooth acceleration and deceleration

            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float

                // Update the gradient shader dynamically
                paint.shader = LinearGradient(
                    animatedValue, 0f, animatedValue + width, height.toFloat(), intArrayOf(
                        ContextCompat.getColor(context, R.color.primary_color_05),
                        ContextCompat.getColor(context, R.color.primary_color_10), // Highlight
                        ContextCompat.getColor(context, R.color.primary_color_05),
                    ), floatArrayOf(0.2f, 0.5f, 0.8f), Shader.TileMode.CLAMP
                )

                // Redraw the ShapeDrawable
                invalidate()
            }
        }

        shimmerAnimator.start()

        val pulseAnimatorX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.05f, 1f).apply {
            duration = 1000L
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val pulseAnimatorY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.05f, 1f).apply {
            duration = 1000L
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        // Combine animations
        val animatorSet = AnimatorSet().apply {
            playTogether(shimmerAnimator, pulseAnimatorX, pulseAnimatorY)
            start()
        }

        // Save the animator set in the tag to stop later
        setTag(R.string.animator_tag, animatorSet)
    }
}

fun View.stopSkeletonAnimation() {
    val animatorSet = getTag(R.string.animator_tag) as? AnimatorSet
    animatorSet?.apply {
        end()
        cancel()
    }
    setTag(R.string.animator_tag, null) // Clear the tag
}

fun View.saveOriginalPadding() {
    val paddingData = intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
    setTag(R.string.padding_tag, paddingData)
}

fun View.restoreOriginalPadding() {
    val paddingData = getTag(R.string.padding_tag) as? IntArray
    if (paddingData != null) {
        setPadding(paddingData[0], paddingData[1], paddingData[2], paddingData[3])
    }
    setTag(R.string.padding_tag, null) // Clear the tag
}