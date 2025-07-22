package com.belive.dating.helpers.helper_views.skeleton

import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.helpers.helper_views.card_swiper.CardStackLayoutManager
import com.belive.dating.helpers.helper_views.card_swiper.Direction
import com.belive.dating.helpers.helper_views.card_swiper.StackFrom
import com.belive.dating.helpers.helper_views.card_swiper.SwipeableMethod
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

open class SkeletonRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    private var skeletonAdapter: SkeletonAdapter? = null
    private var originalAdapter: Adapter<*>? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SkeletonRecyclerView)

        // Get orientation (vertical or horizontal)

        val layoutManagerType =
            typedArray.getString(R.styleable.SkeletonRecyclerView_layoutManagerType)
        if (layoutManagerType.equals("grid")) {
            // Get span count for GridLayoutManager (if it's a grid)
            val spanCount = typedArray.getInt(R.styleable.SkeletonRecyclerView_spanCount, 1)
            val isGrid = spanCount > 1
            val gridLayoutManager = if (isGrid) {
                GridLayoutManager(context, spanCount)
            } else {
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
            // Set the layout manager dynamically
            this.layoutManager = gridLayoutManager
        } else if (layoutManagerType.equals("flex")) {
            val layoutManager = FlexboxLayoutManager(context)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.CENTER
            this.layoutManager = layoutManager
        } else if (layoutManagerType.equals("card-stack")) {
            val layoutManager = CardStackLayoutManager(context)
            layoutManager.setStackFrom(StackFrom.None)
            layoutManager.setVisibleCount(6)
            layoutManager.setTranslationInterval(8.0f)
            layoutManager.setScaleInterval(0.95f)
            layoutManager.setSwipeThreshold(0.3f)
            layoutManager.setMaxDegree(20.0f)
            layoutManager.setDirections(Direction.HORIZONTAL)
            layoutManager.setCanScrollHorizontal(true)
            layoutManager.setCanScrollVertical(true)
            layoutManager.setSwipeableMethod(SwipeableMethod.None)
            layoutManager.setOverlayInterpolator(LinearInterpolator())

            this.layoutManager = layoutManager
        } else {
            val orientation = typedArray.getInt(R.styleable.SkeletonRecyclerView_orientation, 0)
            val layoutManager = if (orientation == 0) {
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            } else {
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            this.layoutManager = layoutManager
        }

        // Get item layout resource
        val listItemResId = typedArray.getResourceId(R.styleable.SkeletonRecyclerView_listItem, 0)

        // Get item count (used for skeleton loading)
        val itemCount = typedArray.getInt(R.styleable.SkeletonRecyclerView_itemCount, 0)

        // Set the skeleton adapter initially
        if (listItemResId != 0) {
            skeletonAdapter = SkeletonAdapter(listItemResId, itemCount)
        }

        typedArray.recycle()
    }

    /**
     * Start showing skeleton loading.
     */
    fun startSkeletonLoading() {
        // Save the current adapter
        if (originalAdapter == null) {
            originalAdapter = adapter
        }
        // Set the skeleton adapter
        adapter = skeletonAdapter
    }

    /**
     * Stop showing skeleton loading and set the original adapter.
     * @param newAdapter The actual data adapter to be set (optional).
     */
    fun stopSkeletonLoading(newAdapter: Adapter<*>? = null) {
        adapter = newAdapter ?: originalAdapter
        originalAdapter = null // Clear reference to avoid memory leaks
    }
}
