package com.demo.movie.tmdb.app.utils

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.movie.tmdb.app.R

class ShimmerDrawable : GradientDrawable() {
    private var shimmerAnimation: ValueAnimator? = null
    private var translateX: Float = 0f

    // Refined colors for ultra-smooth gradient
    private val shimmerColor = ContextCompat.getColor(getKoinContext(), R.color.shimmerColor)
    private val baseColor = ContextCompat.getColor(getKoinContext(), R.color.baseColor)
    private val midColor = ContextCompat.getColor(getKoinContext(), R.color.midColor)
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    init {
        startShimmerAnimation()
    }

    private fun startShimmerAnimation() {
        shimmerAnimation?.cancel()
        shimmerAnimation = ValueAnimator.ofFloat(-1f, 1f).apply {
            duration = 2500  // Longer duration for smoother movement
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            // Custom interpolator for ultra-smooth movement
            interpolator = PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f)

            addUpdateListener { animator ->
                translateX = animator.animatedValue as Float
                invalidateSelf()
            }
            start()
        }
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()

        // Draw base color
        paint.shader = null
        paint.color = baseColor
        canvas.drawRect(bounds, paint)

        // Create enhanced gradient for ultra-smooth shimmer effect
        val gradientWidth = width * 1.5f  // Wider gradient for smoother transition
        val gradient = LinearGradient(
            translateX * width - gradientWidth / 2,
            0f,
            translateX * width + gradientWidth / 2,
            height,
            intArrayOf(
                baseColor,
                midColor,
                shimmerColor,
                shimmerColor,  // Double peak for smoother center
                midColor,
                baseColor,
            ),
            floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f),  // More evenly distributed
            Shader.TileMode.CLAMP
        )

        // Apply additional smoothing to the paint
        paint.apply {
            shader = gradient
            maskFilter = BlurMaskFilter(
                height * 0.1f,  // Subtle blur based on height
                BlurMaskFilter.Blur.NORMAL
            )
        }

        canvas.drawRect(bounds, paint)
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        val changed = super.setVisible(visible, restart)
        if (visible) {
            startShimmerAnimation()
        } else {
            shimmerAnimation?.cancel()
        }
        return changed
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        if (isVisible) {
            startShimmerAnimation()
        }
    }

    // Clean up resources
    fun dispose() {
        shimmerAnimation?.cancel()
        shimmerAnimation = null
    }
}

// SkeletonLoader.kt
class SkeletonLoader {
    private val originalStates = mutableMapOf<Int, ViewState>()

    private data class ViewState(
        val text: CharSequence? = null,
        val background: Drawable? = null,
        val backgroundTint: ColorStateList? = null,
        val imageTint: ColorStateList? = null
    )

    fun showSkeleton(view: View, listItem: Int? = null, itemCount: Int = 10) {
        if (view is RecyclerView) {
            setupRecyclerViewSkeleton(view, listItem!!, itemCount)
            return
        }
        when (view) {
            is ViewGroup -> handleViewGroupSkeleton(view)
            else -> applySkeleton(view)
        }
    }

    fun hideSkeleton(view: View) {
        if (view is RecyclerView) return  // Exclude RecyclerView from restoring skeleton
        when (view) {
            is ViewGroup -> handleViewGroupRestore(view)
            else -> restoreOriginalState(view)
        }
    }

    private fun handleViewGroupSkeleton(viewGroup: ViewGroup) {
        applySkeleton(viewGroup)
        for (i in 0 until viewGroup.childCount) {
            when (val child = viewGroup.getChildAt(i)) {
                is ViewGroup -> handleViewGroupSkeleton(child)
                else -> applySkeleton(child)
            }
        }
    }

    private fun handleViewGroupRestore(viewGroup: ViewGroup) {
        restoreOriginalState(viewGroup)
        for (i in 0 until viewGroup.childCount) {
            when (val child = viewGroup.getChildAt(i)) {
                is ViewGroup -> handleViewGroupRestore(child)
                else -> restoreOriginalState(child)
            }
        }
    }

    private fun setupRecyclerViewSkeleton(recyclerView: RecyclerView, listItem: Int, itemCount: Int) {
        val layoutManager = recyclerView.layoutManager ?: LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.tag = "skeleton"
        recyclerView.adapter = object : RecyclerView.Adapter<SkeletonViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeletonViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(listItem, parent, false)
                return SkeletonViewHolder(view)
            }
            override fun onBindViewHolder(holder: SkeletonViewHolder, position: Int) {
                holder.bind()
            }
            override fun getItemCount(): Int = itemCount
        }
    }

    private class SkeletonViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            SkeletonBindingAdapter.setSkeletonVisible(view, true)
        }
    }

    private fun applySkeleton(view: View) {
        val viewId = view.id
        if (viewId == View.NO_ID) return

        // Store original state if not already stored
        if (!originalStates.containsKey(viewId)) {
            originalStates[viewId] = ViewState(
                text = (view as? TextView)?.text,
                background = view.background,
                backgroundTint = view.backgroundTintList,
                imageTint = (view as? ImageView)?.imageTintList
            )
        }

        // Apply skeleton effect
        when (view) {
            is TextView -> {
                view.text = ""
            }
            is ImageView -> {
                view.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
            }
        }

        // Apply shimmer background
        view.background = ShimmerDrawable()
    }

    private fun restoreOriginalState(view: View) {
        val viewId = view.id
        if (viewId == View.NO_ID) return

        originalStates[viewId]?.let { state ->
            when (view) {
                is TextView -> {
                    state.text?.let { view.text = it }
                }
                is ImageView -> {
                    view.clearColorFilter()
                    state.imageTint?.let { view.imageTintList = it }
                }
            }
            view.background = state.background
            view.backgroundTintList = state.backgroundTint
        }
    }
}

// SkeletonBindingAdapter.kt
object SkeletonBindingAdapter {
    private val skeletonLoader = SkeletonLoader()

    @JvmStatic
    @BindingAdapter("isSkeletonVisible", "skeletonItem", "skeletonItemCount", requireAll = false)
    fun setSkeletonVisible(view: View, isVisible: Boolean, listItem: Int? = null, itemCount: Int? = null) {
        if (isVisible) {
            skeletonLoader.showSkeleton(view, listItem, itemCount ?: 10)
        } else {
            skeletonLoader.hideSkeleton(view)
        }
    }
}