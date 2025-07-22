package com.belive.dating.helpers.helper_functions.swipe_replay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.api.user.models.friend_chat.Message
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import kotlin.math.abs
import kotlin.math.ceil
import androidx.core.graphics.toColorInt

interface SwipeControllerActions {
    fun showReplyUI(position: Int)
}

class SwipeToReplayCallback(
    private val context: Context,
    private val swipeControllerActions: SwipeControllerActions,
    private val chatList: ArrayList<Message>
) :
    ItemTouchHelper.Callback() {

//    private lateinit var imageDrawable: Drawable
//    private lateinit var shareRound: Drawable

    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    private lateinit var itemView: ConstraintLayout
    private lateinit var itemRoot: LinearLayout
    private var dX = 0f

    private var replyButtonProgress: Float = 0.toFloat()
    private var lastReplyButtonAnimationTime: Long = 0
    private var swipeBack = false
    private var isVibrate = false
    private var startTracking = false
    private val backgroundPaint = Paint().apply {
        color = "#57454545".toColorInt()
    } // Set your desired background color here

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        var typ = 0

        if (viewHolder.bindingAdapterPosition >= chatList.size) {
            return ACTION_STATE_IDLE
        }
        typ = chatList[viewHolder.bindingAdapterPosition].mediaType
        logger("TAG", "getMovementFlags: ============" + typ)
//        val swipeFlags = ACTION_STATE_IDLE
        val swipeFlags = if (typ == 1) {
            //text
            itemView = viewHolder.itemView.findViewById(R.id.main)
            itemRoot = viewHolder.itemView.findViewById(R.id.layout_message)
            if (chatList[viewHolder.bindingAdapterPosition].sendBy == getUserPrefs().userId) {
                // me
                ItemTouchHelper.LEFT
            } else {
                //other
                ItemTouchHelper.RIGHT
            }
        } else if (typ == 2) {
            itemView = viewHolder.itemView.findViewById(R.id.main)
            itemRoot = viewHolder.itemView.findViewById(R.id.layout_message)
            //image
            if (chatList[viewHolder.bindingAdapterPosition].sendBy == getUserPrefs().userId) {
                // me
                ItemTouchHelper.LEFT
            } else {
                //other
                ItemTouchHelper.RIGHT
            }
        } else if (typ == 3) {
            itemView = viewHolder.itemView.findViewById(R.id.main)
            itemRoot = viewHolder.itemView.findViewById(R.id.layout_message)
            //video
            if (chatList[viewHolder.bindingAdapterPosition].sendBy == getUserPrefs().userId) {
                // me
                ItemTouchHelper.LEFT
            } else {
                //other
                ItemTouchHelper.RIGHT
            }
        } else {
            ACTION_STATE_IDLE
        }
        return makeMovementFlags(ACTION_STATE_IDLE, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        logger("SwipeToReplayCallback", "onSwiped: ")
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        if (actionState == ACTION_STATE_SWIPE) {
            setTouchListener(recyclerView, viewHolder)
        }

        val maxSwipeDistance = 200f // Change this value as needed
        val limitedDx = dX.coerceAtMost(maxSwipeDistance).coerceAtLeast(-maxSwipeDistance)

        if (itemRoot.translationX < convertTodp(80) || dX < this.dX) {


//            val imgSeenView = viewHolder.itemView.findViewById<ImageView>(R.id.imgProfile)


//            val limitedDx = if (dX < -maxSwipeDistance) -maxSwipeDistance else dX


            if (chatList[viewHolder.bindingAdapterPosition].isSelected) {
//                if (limitedDx > 0) { // Swipe right
//                    c.drawRect(
//                        itemRoot.left.toFloat(),
//                        itemRoot.top.toFloat(),
//                        itemRoot.left+limitedDx,
//                        itemRoot.bottom.toFloat(),
//                        backgroundPaint
//                    )
//
//
//
//                } else { // Swipe left
//                    c.drawRect(
//                        itemRoot.right + limitedDx,
//                        itemRoot.top.toFloat(),
//                        itemRoot.right.toFloat(),
//                        itemRoot.bottom.toFloat(),
//                        backgroundPaint
//                    )
//                }

            }
            itemRoot.translationX = limitedDx
        }

        super.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
        this.dX = dX
        startTracking = true

        currentItemViewHolder = viewHolder
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        recyclerView.setOnTouchListener { _, event ->
            swipeBack =
                event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                if (abs(itemRoot.translationX) >= 100f) {
                    swipeControllerActions.showReplyUI(viewHolder.bindingAdapterPosition)
                }
            }
            false
        }
    }

    private fun convertTodp(pixel: Int): Int {
        return dp(pixel.toFloat(), context)
    }

    private var density = 1f

    fun dp(value: Float, context: Context): Int {
        if (density == 1f) {
            checkDisplaySize(context)
        }
        return if (value == 0f) {
            0
        } else ceil((density * value).toDouble()).toInt()
    }

    private fun checkDisplaySize(context: Context) {
        try {
            density = context.resources.displayMetrics.density
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}