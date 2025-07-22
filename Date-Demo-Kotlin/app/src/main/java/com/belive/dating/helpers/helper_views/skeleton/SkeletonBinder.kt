package com.belive.dating.helpers.helper_views.skeleton

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.juky.squircleview.views.SquircleConstraintLayout
import com.belive.dating.R
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.gsonString
import com.belive.dating.helpers.helper_views.card_swiper.CardStackLayoutManager
import com.belive.dating.helpers.helper_views.card_swiper.Direction
import com.belive.dating.helpers.helper_views.card_swiper.StackFrom
import com.belive.dating.helpers.helper_views.card_swiper.SwipeableMethod
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class ShimmerDrawable(private val view: View) : Drawable() {
    private var shimmerAnimation: ValueAnimator? = null
    private var translateFactor: Float = 0f
    private val frameRateMs = 16L // 60 FPS
    private var lastFrameTime: Long = 0

    // Define shimmer colors
    private val color05 = ContextCompat.getColor(view.context, R.color.primary_color_05)
    private val color06 = ContextCompat.getColor(view.context, R.color.primary_color_06)
    private val color07 = ContextCompat.getColor(view.context, R.color.primary_color_07)
    private val color08 = ContextCompat.getColor(view.context, R.color.primary_color_08)
    private val color09 = ContextCompat.getColor(view.context, R.color.primary_color_09)
    private val color10 = ContextCompat.getColor(view.context, R.color.primary_color_10)
    private val color11 = ContextCompat.getColor(view.context, R.color.primary_color_11)
    private val color12 = ContextCompat.getColor(view.context, R.color.primary_color_12)
    private val color13 = ContextCompat.getColor(view.context, R.color.primary_color_13)
    private val color14 = ContextCompat.getColor(view.context, R.color.primary_color_14)
    private val color15 = ContextCompat.getColor(view.context, R.color.primary_color_15)

    // Expanded gradient colors for smoother blending
    private val colors = intArrayOf(
        color05, color06, color07, color08, color09, color10, color11, color12, color13, color14, color15,
        color14, color13, color12, color11, color10, color09, color08, color07, color06, color05,
    )

    // Positions to distribute colors evenly
    private val positions = floatArrayOf(
        0f, 0.05f, 0.10f, 0.15f, 0.20f, 0.25f, 0.30f, 0.35f, 0.40f, 0.45f, 0.50f,
        0.55f, 0.60f, 0.65f, 0.70f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f, 1f,
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
        isDither = true
        isFilterBitmap = true
        isAntiAlias = true
    }

    init {
        startShimmerAnimation()
    }

    private fun startShimmerAnimation() {
        shimmerAnimation?.cancel()
        shimmerAnimation = ValueAnimator.ofFloat(-1f, 1f).apply { // Adjusted end offset
            duration = 2000L // Smooth movement
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFrameTime >= frameRateMs) {
                    translateFactor = animator.animatedValue as Float
                    invalidateSelf()
                    lastFrameTime = currentTime
                }
            }
            start()
        }
    }

    override fun draw(canvas: Canvas) {
        val width = view.measuredWidth.toFloat()
        val height = view.measuredHeight.toFloat()
        if (width <= 0 || height <= 0) return

        val diagonalDistance = sqrt((width * width + height * height).toDouble()).toFloat()
        val shimmerSize = diagonalDistance * 1.3f // Cover full diagonal smoothly

        // Offset start and end to keep shimmer visible
        val startX = (translateFactor + 0.5f) * width // Adjusted for better visibility
        val startY = (translateFactor + 0.5f) * height // Adjusted for better visibility

        // Creating a 45-degree diagonal gradient with optimized range
        val gradient = LinearGradient(
            startX - shimmerSize * 0.5f, // X Start
            startY - shimmerSize * 0.5f, // Y Start
            startX + shimmerSize * 0.5f, // X End
            startY + shimmerSize * 0.5f, // Y End
            colors, positions, Shader.TileMode.CLAMP
        )

        paint.shader = gradient
        canvas.drawRect(bounds, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        if (isVisible) startShimmerAnimation()
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        val changed = super.setVisible(visible, restart)
        if (visible) {
            startShimmerAnimation()
        } else {
            dispose()
        }
        return changed
    }

    private fun dispose() {
        shimmerAnimation?.cancel()
        shimmerAnimation = null
    }
}

// SkeletonLoader.kt
class SkeletonLoader {
    private val originalStates = mutableMapOf<Int, ViewState>()

    private data class ViewState(
        val name: String? = null,
        val text: CharSequence? = null,
        val textColor: Int? = null,
        val minWidth: Int? = null,
        val background: Drawable? = null,
        val backgroundTint: ColorStateList? = null,
        val imageTint: ColorStateList? = null,
        val image: Drawable? = null,
        val backgroundGradientDrawable: GradientDrawable? = null,
        val borderColor: Int? = null,
    )

    fun showSkeleton(
        view: View,
        listItem: Int? = null,
        layoutManagerType: String? = null,
        orientation: String? = null,
        spanCount: Int? = null,
        itemCount: Int? = null,
    ) {
        if (view is RecyclerView) {
            setupRecyclerViewSkeleton(view, listItem!!, layoutManagerType, orientation, spanCount, itemCount)
            return
        }

        when (view) {
            is ViewGroup -> handleViewGroupSkeleton(view)
            else -> {
                applySkeleton(view)
            }
        }
    }

    fun hideSkeleton(view: View) {

        when (view) {
            is RecyclerView -> removeRecyclerViewSkeleton(view)
            is ViewGroup -> handleViewGroupRestore(view)
            else -> restoreOriginalState(view)
        }
    }

    private fun removeRecyclerViewSkeleton(view: RecyclerView) {

    }

    private fun handleViewGroupSkeleton(viewGroup: ViewGroup) {
        if (!viewGroup.isVisible) return

        viewGroup.blockTouches(true)

        applySkeleton(viewGroup)

        for (i in 0 until viewGroup.childCount) {
            when (val child = viewGroup.getChildAt(i)) {
                is ViewGroup -> handleViewGroupSkeleton(child)
                else -> {
                    applySkeleton(child)
                }
            }
        }
    }

    private fun handleViewGroupRestore(viewGroup: ViewGroup) {
        if (!viewGroup.isVisible) return

        viewGroup.blockTouches(false)
        restoreOriginalState(viewGroup)

        for (i in 0 until viewGroup.childCount) {
            when (val child = viewGroup.getChildAt(i)) {
                is ViewGroup -> handleViewGroupRestore(child)
                else -> restoreOriginalState(child)
            }
        }

        originalStates.remove(viewGroup.id)
    }

    private fun setupRecyclerViewSkeleton(
        recyclerView: RecyclerView,
        listItem: Int,
        layoutManagerType: String? = null,
        orientation: String? = null,
        spanCount: Int?,
        itemCount: Int?,
    ) {
        val layoutManager = when (layoutManagerType) {
            "grid" -> {
                GridLayoutManager(recyclerView.context, spanCount ?: 2)
            }

            "flex" -> {
                FlexboxLayoutManager(recyclerView.context).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.CENTER
                }
            }

            "card-stack" -> {
                CardStackLayoutManager(recyclerView.context).apply {
                    setStackFrom(StackFrom.None)
                    setVisibleCount(1)
                    setTranslationInterval(8.0f)
                    setScaleInterval(0.95f)
                    setSwipeThreshold(0.3f)
                    setMaxDegree(20.0f)
                    setDirections(Direction.HORIZONTAL)
                    setCanScrollHorizontal(true)
                    setCanScrollVertical(true)
                    setSwipeableMethod(SwipeableMethod.None)
                    setOverlayInterpolator(LinearInterpolator())
                }
            }

            else -> {
                if (orientation == "horizontal") {
                    LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
                } else {
                    LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
                }
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.tag = "skeleton"
        recyclerView.adapter = object : RecyclerView.Adapter<SkeletonViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeletonViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(listItem, parent, false)
                return SkeletonViewHolder(recyclerView, view)
            }

            override fun onBindViewHolder(holder: SkeletonViewHolder, position: Int) {
                holder.bind()
            }

            override fun getItemCount(): Int = itemCount ?: 10
        }
    }

    private class SkeletonViewHolder(val recyclerView: RecyclerView, val view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            attachTagRecursively(recyclerView, "RecyclerView")
            SkeletonBindingAdapter.setSkeletonVisible(view, true)
        }

        fun attachTagRecursively(view: View, tag: Any) {
            val viewId = if (view.id == View.NO_ID) View.generateViewId() else view.id
            view.id = viewId
            view.tag = "${tag}${view.id}"
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    attachTagRecursively(view.getChildAt(i), tag)
                }
            }
        }
    }

    private fun applySkeleton(view: View) {
        if (!view.isVisible) return

        view.blockTouches(true)

        if (view.getTag(R.string.skeleton_tag) == true) return

        if (view.id == View.NO_ID) {
            view.id = View.generateViewId()
        }
        val viewId = view.id

        view.post {
            // Store original state if not already stored
            if (!originalStates.containsKey(viewId)) {
                originalStates[viewId] = ViewState(
                    name = view.javaClass.simpleName,
                    text = (view as? TextView)?.text,
                    textColor = (view as? TextView)?.currentTextColor,
                    minWidth = (view as? TextView)?.minWidth,
                    background = view.background,
                    backgroundTint = view.backgroundTintList,
                    imageTint = (view as? ImageView)?.imageTintList,
                    backgroundGradientDrawable = (view as? SquircleConstraintLayout)?.style?.backgroundGradientDrawable,
                    borderColor = (view as? SquircleConstraintLayout)?.style?.borderColor
                )
            }

            // Apply skeleton effect
            when (view) {
                is TextView -> {
                    view.minWidth = view.measuredWidth
                    view.setTextColor(ColorStateList.valueOf(Color.TRANSPARENT))
                    view.text = ""
                }

                is SquircleConstraintLayout -> {
                    view.style.backgroundGradientDrawable = null
                    view.style.borderColor = Color.TRANSPARENT
                }

                is ImageView -> {
                    view.imageTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                }
            }

            if (view.tag == view.resources.getString(R.string.skip_skeleton_animation)) {
                view.background = null
            } else {
                // Apply shimmer background
                when (view) {
                    is MaterialButton -> {
                        view.applySkeletonEffectToView()
                    }

                    is SquircleConstraintLayout -> {
                        view.style.backgroundGradientDrawable = convertToGradientDrawable(ShimmerDrawable(view))
                    }

                    else -> {
                        view.background = ShimmerDrawable(view)
                    }
                }
                view.setTag(R.string.skeleton_tag, true)
            }
        }
    }

    private fun convertToGradientDrawable(drawable: Drawable?): GradientDrawable? {
        return when (drawable) {
            is GradientDrawable -> drawable // Already a GradientDrawable, return as is
            is ColorDrawable -> GradientDrawable().apply { setColor(drawable.color) } // Convert ColorDrawable
            else -> null // Other drawables like BitmapDrawable, VectorDrawable cannot be directly converted
        }
    }

    private fun View.applySkeletonEffectToView() {
        post {
            try {
                if (width == 0 || height == 0) return@post

                // Define shimmer colors
                val color05 = ContextCompat.getColor(context, R.color.primary_color_05)
                val color06 = ContextCompat.getColor(context, R.color.primary_color_06)
                val color07 = ContextCompat.getColor(context, R.color.primary_color_07)
                val color08 = ContextCompat.getColor(context, R.color.primary_color_08)
                val color09 = ContextCompat.getColor(context, R.color.primary_color_09)
                val color10 = ContextCompat.getColor(context, R.color.primary_color_10)
                val color11 = ContextCompat.getColor(context, R.color.primary_color_11)
                val color12 = ContextCompat.getColor(context, R.color.primary_color_12)
                val color13 = ContextCompat.getColor(context, R.color.primary_color_13)
                val color14 = ContextCompat.getColor(context, R.color.primary_color_14)
                val color15 = ContextCompat.getColor(context, R.color.primary_color_15)

                // Create a gradient that transitions from color05 up to color15 and back to color05
                val colors = intArrayOf(
                    color05, color06, color07, color08, color09, color10, color11, color12, color13, color14, color15,
                    color14, color13, color12, color11, color10, color09, color08, color07, color06, color05,
                )

                // Evenly distribute the color stops
                val positions = floatArrayOf(
                    0f, 0.05f, 0.10f, 0.15f, 0.20f, 0.25f, 0.30f, 0.35f, 0.40f, 0.45f, 0.50f,
                    0.55f, 0.60f, 0.65f, 0.70f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f, 1f,
                )

                val viewWidth = width.toFloat()
                val viewHeight = height.toFloat()

                // Calculate the diagonal distance of the view (covers the view at 45°)
                val diagonalDistance = sqrt(viewWidth * viewWidth + viewHeight * viewHeight)
                // Expand the gradient to ensure full coverage during animation
                val shimmerSize = diagonalDistance * 1.3f

                // Compute the center of the view
                val centerX = viewWidth / 2
                val centerY = viewHeight / 2

                // Define the initial gradient positions for a 45° diagonal gradient:
                val initialStartX = centerX - shimmerSize * 0.5f
                val initialStartY = centerY - shimmerSize * 0.5f
                val initialEndX = centerX + shimmerSize * 0.5f
                val initialEndY = centerY + shimmerSize * 0.5f

                // Create a Paint with an initial 45° diagonal LinearGradient shader
                val paint = Paint().apply {
                    shader = LinearGradient(
                        initialStartX, initialStartY, initialEndX, initialEndY, colors, positions, Shader.TileMode.CLAMP
                    )
                }

                // Use a ShapeDrawable to set as the view's background
                val shapeDrawable = object : ShapeDrawable() {
                    override fun draw(canvas: Canvas) {
                        canvas.drawRect(bounds, paint)
                    }
                }
                background = shapeDrawable

                // Create an animator that moves the gradient along the 45° line.
                // We animate the offset along both X and Y directions simultaneously.
                // The offset range is set from -shimmerSize/2 to shimmerSize/2 to ensure the highlight remains visible.
                val shimmerAnimator = ValueAnimator.ofFloat(-shimmerSize * 0.5f, shimmerSize * 0.5f).apply {
                    duration = 2000L
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animation ->
                        val offset = animation.animatedValue as Float
                        // Update the shader so that the gradient moves along a 45° line
                        val startXAnim = centerX - shimmerSize * 0.5f + offset
                        val startYAnim = centerY - shimmerSize * 0.5f + offset
                        val endXAnim = centerX + shimmerSize * 0.5f + offset
                        val endYAnim = centerY + shimmerSize * 0.5f + offset
                        paint.shader = LinearGradient(
                            startXAnim, startYAnim, endXAnim, endYAnim, colors, positions, Shader.TileMode.CLAMP
                        )
                        invalidate() // Redraw the drawable with the updated shader values
                    }
                }
                shimmerAnimator.start()
            } catch (e: Exception) {
                catchLog("applySkeletonEffectToView: ${gsonString(e)}")
            }
        }
    }

    private fun View.blockTouches(isSkeleton: Boolean) {
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

    private fun restoreOriginalState(view: View) {
        if (!view.isVisible) return

        view.blockTouches(false)

        val viewId = view.id
        view.setTag(R.string.skeleton_tag, null)

        originalStates[viewId]?.let { state ->
            when (view) {
                is TextView -> {
                    view.text = state.text
                    view.minWidth = state.minWidth ?: 0
                    view.setTextColor(state.textColor ?: 0)
                }

                is SquircleConstraintLayout -> {
                    view.style.backgroundGradientDrawable = state.backgroundGradientDrawable
                    view.style.borderColor = state.borderColor ?: Color.TRANSPARENT
                }

                is ImageView -> {
                    view.clearColorFilter()
                    view.imageTintList = state.imageTint
                }
            }
            view.background = state.background
            view.backgroundTintList = state.backgroundTint

            originalStates.remove(viewId)
        }
    }
}

// SkeletonBindingAdapter.kt
object SkeletonBindingAdapter {
    private val skeletonLoader = SkeletonLoader()

    private val skeletonJobs = mutableMapOf<View, Job?>()
    private val lastExecutionTimes = mutableMapOf<View, Long>()
    private const val DEBOUNCE_TIME = 1000L // 1 second debounce

    @JvmStatic
    @BindingAdapter(
        "tools:isSkeletonVisible",
        "tools:skeletonItem",
        "tools:skeletonLayoutManager",
        "tools:skeletonOrientation",
        "tools:skeletonSpanCount",
        "tools:skeletonItemCount",
        requireAll = false,
    )
    fun setSkeletonVisible(
        view: View,
        isVisible: Boolean,
        listItem: Int? = null,
        layoutManagerType: String? = null,
        orientation: String? = null,
        spanCount: Int? = null,
        itemCount: Int? = null,
    ) {
        val currentTime = System.currentTimeMillis()
        val lastTime = lastExecutionTimes[view] ?: 0L

        if (currentTime - lastTime >= DEBOUNCE_TIME) {
            // Execute immediately if it's the first call or debounce time has passed
            lastExecutionTimes[view] = currentTime
            executeSkeleton(view, isVisible, listItem, layoutManagerType, orientation, spanCount, itemCount)
        } else {
            // Debounce per view
            skeletonJobs[view]?.cancel()
            skeletonJobs[view] = CoroutineScope(Dispatchers.Main).launch {
                delay(DEBOUNCE_TIME)
                lastExecutionTimes[view] = System.currentTimeMillis()
                executeSkeleton(view, isVisible, listItem, layoutManagerType, orientation, spanCount, itemCount)

                skeletonJobs.remove(view)
            }
        }
    }

    private fun executeSkeleton(
        view: View,
        isVisible: Boolean,
        listItem: Int?,
        layoutManagerType: String?,
        orientation: String?,
        spanCount: Int?,
        itemCount: Int?,
    ) {
        if (isVisible) {
            skeletonLoader.showSkeleton(view, listItem, layoutManagerType, orientation, spanCount, itemCount)
        } else {
            skeletonLoader.hideSkeleton(view)
        }
    }
}