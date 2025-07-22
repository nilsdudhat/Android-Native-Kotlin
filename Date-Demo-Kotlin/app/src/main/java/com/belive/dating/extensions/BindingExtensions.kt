package com.belive.dating.extensions

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.SweepGradient
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import app.juky.squircleview.views.SquircleConstraintLayout
import com.belive.dating.R
import com.belive.dating.activities.dashboard.fragments.home.HomeViewModel
import com.belive.dating.activities.introduction.upload_photo.Reject
import com.belive.dating.extensions.loadSelectedImage
import com.belive.dating.helpers.helper_views.skeleton.showSkeleton
import com.belive.dating.payment.ProductType
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import eightbitlab.com.blurview.BlurView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@BindingAdapter("tools:showSkeleton")
fun ViewGroup.displaySkeleton(isSkeleton: Boolean) {
    showSkeleton(isSkeleton)
}

@BindingAdapter("tools:visibility")
fun View.setVisibility(value: Boolean?) {
    if (value == null) return
    visibility = if (value) View.VISIBLE else View.GONE
}

@BindingAdapter("tools:src")
fun ImageView.setImageDrawable(@DrawableRes drawable: Int) {
    setImageResource(drawable)
}

@BindingAdapter("tools:transactionDate")
fun setTransactionDate(view: TextView, inputDate: String) {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Parse as UTC

        val outputFormat = SimpleDateFormat("dd MMMM, yyyy (HH:mm:ss)", Locale.US)
        outputFormat.timeZone = TimeZone.getDefault() // Convert to device local time

        val date: Date = inputFormat.parse(inputDate)!! // Convert String to Date
        val formattedDate = outputFormat.format(date) // Convert Date to formatted String

        view.text = formattedDate // Set text in TextView
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@BindingAdapter("tools:tagGradientDrawable")
fun View.setTagGradientDrawable(productType: ProductType?) {
    background = when (productType) {
        ProductType.LIKE -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#FD3E5E".toColorInt(),
                    "#DF00C9".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        ProductType.SUPER_LIKE -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#0059FF".toColorInt(),
                    "#00B2FF".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        ProductType.REWIND -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#6DFF00".toColorInt(),
                    "#C2FFA9".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        else -> {
            ColorDrawable().apply {
                color = ContextCompat.getColor(getKoinContext(), R.color.white)
            }
        }
    }
}

@BindingAdapter("tools:gradientDrawable")
fun View.setGradientDrawable(productType: ProductType?) {
    background = when (productType) {
        ProductType.GOLD -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#F6E37A".toColorInt(),
                    "#F4E178".toColorInt(),
                    "#C9A33D".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.BL_TR
            }
        }

        ProductType.PLATINUM -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#F2F2F2".toColorInt(),
                    "#D4DEE5".toColorInt(),
                    "#7A96AC".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.BL_TR
            }
        }

        ProductType.LIFETIME -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#F6E37A".toColorInt(),
                    "#F4E178".toColorInt(),
                    "#C9A33D".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.BL_TR
            }
        }

        ProductType.LIKE -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#FD3E5E".toColorInt(),
                    "#DF00C9".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        ProductType.SUPER_LIKE -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#0059FF".toColorInt(),
                    "#00B2FF".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        ProductType.REWIND -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#6DFF00".toColorInt(),
                    "#C2FFA9".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        ProductType.BOOST -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#FFCC00".toColorInt(),
                    "#F9EF63".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        ProductType.DIAMOND -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#3CBADF".toColorInt(),
                    "#AAEDFF".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        else -> {
            ColorDrawable().apply {
                color = "#2C2C2E".toColorInt()
            }
        }
    }
}

@BindingAdapter("tools:tagLightGradientDrawable")
fun View.setTagLightGradientDrawable(productType: ProductType?) {
    background = when (productType) {
        ProductType.REWIND -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#6DFF00".toColorInt(),
                    "#C2FFA9".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        ProductType.BOOST -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#FFCC00".toColorInt(),
                    "#F9EF63".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        ProductType.DIAMOND -> {
            GradientDrawable().apply {
                colors = intArrayOf(
                    "#3CBADF".toColorInt(),
                    "#AAEDFF".toColorInt(),
                )
                shape = GradientDrawable.RECTANGLE
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            }
        }

        else -> {
            ColorDrawable().apply {
                color = ContextCompat.getColor(getKoinContext(), R.color.black)
            }
        }
    }
}

@BindingAdapter("tools:showSkeleton")
fun View.displaySkeleton(isSkeleton: Boolean) {
    showSkeleton(isSkeleton)
}

@BindingAdapter("tools:backgroundTint")
fun View.applyBackgroundTint(@ColorRes color: Int) {
    backgroundTintList = context.getColorStateList(color)
}

@BindingAdapter("tools:subscriptionTitle")
fun ImageView.applySubscriptionTitle(selectedPlanType: ProductType?) {
    if (selectedPlanType == null) {
        return
    }
    if (selectedPlanType == ProductType.GOLD) {
        setImageResource(R.drawable.belive_gold)
    }
    if (selectedPlanType == ProductType.PLATINUM) {
        setImageResource(R.drawable.belive_platinum)
    }
    if (selectedPlanType == ProductType.LIFETIME) {
        setImageResource(R.drawable.belive_lifetime)
    }
}

@BindingAdapter("tools:subscriptionBadge")
fun ImageView.applySubscriptionBadge(selectedPlanType: ProductType?) {
    if (selectedPlanType == null) {
        return
    }
    if (selectedPlanType == ProductType.GOLD) {
        setImageResource(R.drawable.badge_gold)
    }
    if (selectedPlanType == ProductType.PLATINUM) {
        setImageResource(R.drawable.badge_platinum)
    }
    if (selectedPlanType == ProductType.LIFETIME) {
        setImageResource(R.drawable.badge_lifetime)
    }
}

@BindingAdapter("tools:background")
fun View.applyBackground(@DrawableRes resource: Int?) {
    background = if (resource == null) {
        null
    } else {
        ContextCompat.getDrawable(context, resource)
    }
}

@BindingAdapter("tools:mainTitle", "tools:isAI")
fun MaterialToolbar.setMainTitle(activePackage: ProductType?, isAI: Boolean) {
    // Get the Drawables
    val appTitleLogo: Drawable? = ContextCompat.getDrawable(getKoinContext(), R.drawable.app_title_logo)
    val activePlanBadge: Drawable? = if (isAI) {
        ContextCompat.getDrawable(getKoinContext(), R.drawable.ai_match_maker_badge)
    } else if (activePackage == ProductType.GOLD) {
        ContextCompat.getDrawable(getKoinContext(), R.drawable.badge_gold)
    } else if (activePackage == ProductType.PLATINUM) {
        ContextCompat.getDrawable(getKoinContext(), R.drawable.badge_platinum)
    } else if (activePackage == ProductType.LIFETIME) {
        ContextCompat.getDrawable(getKoinContext(), R.drawable.badge_lifetime)
    } else {
        ContextCompat.getDrawable(getKoinContext(), R.drawable.transparent_icon)
    }

    // Resize the Drawables to Fit the Toolbar Title Area
    appTitleLogo?.setBounds(0, 0, appTitleLogo.intrinsicWidth, appTitleLogo.intrinsicHeight)
    activePlanBadge?.setBounds(0, 0, activePlanBadge.intrinsicWidth, activePlanBadge.intrinsicHeight)

    // Create a SpannableString
    val spannableString = SpannableString(" \u00A0  ").apply {
        setSpan("\u00A0", 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    } // Placeholder for two icons

    // Add the First Icon as an ImageSpan
    if (appTitleLogo != null) {
        val firstImageSpan = ImageSpan(appTitleLogo, ImageSpan.ALIGN_BASELINE)
        spannableString.setSpan(firstImageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    // Add the Second Icon as an ImageSpan
    if (activePlanBadge != null) {
        val secondImageSpan = ImageSpan(activePlanBadge, ImageSpan.ALIGN_BASELINE)
        spannableString.setSpan(secondImageSpan, 3, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    spannableString.trim()

    // Set the SpannableString as the Toolbar Title
    title = spannableString

    // Align the title based on the fragment
    isTitleCentered = false
}

@BindingAdapter("tools:nameCounter")
fun TextView.nameCounter(nameLength: Int) {
    text = StringBuilder().append("(").append(nameLength).append("/20)")

    if (nameLength < 3) {
        setTextColor(ContextCompat.getColor(context, R.color.colorError))
    } else {
        setTextColor(ContextCompat.getColor(context, R.color.colorTextHint))
    }
}

@BindingAdapter("tools:aboutMeCounter")
fun TextView.aboutMeCounter(aboutMe: String?) {
    text = StringBuilder().append("(").append(aboutMe?.length ?: 0).append("/100)")

    if ((aboutMe.isNullOrEmpty()) || aboutMe.length < 15) {
        setTextColor(ContextCompat.getColor(context, R.color.colorError))
    } else {
        setTextColor(ContextCompat.getColor(context, R.color.colorTextHint))
    }
}

@BindingAdapter("tools:photoPercentage")
fun TextView.photoPercentage(photoCount: Int?) {
    if ((photoCount != null) && (photoCount == 6)) {
        gone()
        return
    } else {
        visible()
    }

    if ((photoCount == null) || (photoCount == 0)) {
        text = StringBuilder().append("+ 30%")
        return
    }
    text = StringBuilder().append("+ ").apply {
        when (photoCount) {
            1 -> {
                append("25")
            }

            2 -> {
                append("20")
            }

            3 -> {
                append("15")
            }

            4 -> {
                append("10")
            }

            5 -> {
                append("5")
            }
        }
    }.append("%")
}

@BindingAdapter("tools:schoolCounter")
fun TextView.schoolCounter(school: String?) {
    text = StringBuilder().append("(").append(school?.length ?: 0).append("/50)")

    if ((school.isNullOrEmpty()) || school.length < 10) {
        setTextColor(ContextCompat.getColor(context, R.color.colorError))
    } else {
        setTextColor(ContextCompat.getColor(context, R.color.colorTextHint))
    }
}

@BindingAdapter("tools:interestsCounter")
fun TextView.interestsCounter(interestsCount: Int) {
    text = StringBuilder().append("(").append(interestsCount).append("/12)")

    if (interestsCount < 6) {
        setTextColor(ContextCompat.getColor(context, R.color.colorError))
    } else {
        setTextColor(ContextCompat.getColor(context, R.color.colorTextHint))
    }
}

@BindingAdapter("tools:languagesCounter")
fun TextView.languagesCounter(interestsCount: Int) {
    text = StringBuilder().append("(").append(interestsCount).append("/3)")
}

@BindingAdapter("tools:personalityCounter")
fun TextView.personalityCounter(personalityCount: Int) {
    text = StringBuilder().append("(").append(personalityCount).append("/3)")
}

@BindingAdapter("tools:communicationCounter")
fun TextView.communicationCounter(communicationCount: Int) {
    text = StringBuilder().append("(").append(communicationCount).append("/3)")
}

@BindingAdapter("tools:loveTypeCounter")
fun TextView.loveTypeCounter(loveTypeCount: Int) {
    text = StringBuilder().append("(").append(loveTypeCount).append("/3)")
}

@BindingAdapter("tools:unreadCount")
fun TextView.unReadCount(unReadCount: Int) {
    text = if (unReadCount > 99) {
        StringBuilder().append("99+")
    } else {
        StringBuilder().append(unReadCount)
    }
}

@BindingAdapter("tools:lastMessageTime")
fun TextView.lastMessageTime(dateTime: String?) {
    if (dateTime.isNullOrEmpty()) {
        return
    }
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply {
        timeInMillis = iso8601ToMillis(dateTime)
    }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val time = when {
        isSameDay(messageDate, today) -> convertDateToTime(dateTime)
        isSameDay(messageDate, tomorrow) -> "Yesterday"
        else -> dateFormat.format(iso8601ToMillis(dateTime))
    }
    text = StringBuilder().append(time)
}

@BindingAdapter("tools:photosCounter")
fun TextView.photoCounter(photosCount: Int) {
    text = StringBuilder().append("(").append(photosCount).append("/6)")

    if (photosCount < 2) {
        setTextColor(ContextCompat.getColor(context, R.color.colorError))
    } else {
        setTextColor(ContextCompat.getColor(context, R.color.colorTextHint))
    }
}

@BindingAdapter("tools:selectButton")
fun Button.select(isSelect: Boolean) {
    if (isSelect) {
        setTextColor(
            ContextCompat.getColor(
                context,
                R.color.primary_color,
            )
        )
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            0, 0, R.drawable.ic_tick_circle, 0,
        )
        typeface = ResourcesCompat.getFont(context, R.font.urbanist_bold)
    } else {
        setTextColor(
            ContextCompat.getColor(
                context,
                R.color.colorTextHint,
            )
        )
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            null, null, null, null,
        )
        typeface = ResourcesCompat.getFont(context, R.font.urbanist_regular)
    }
}

@BindingAdapter("tools:loadBitmap")
fun ImageView.loadBitmap(bitmap: Bitmap?) {
    if (bitmap == null) {
        getGlide().load(R.color.colorTransparent).into(this)
    } else {
        getGlide().load(bitmap).priority(Priority.IMMEDIATE).downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
            .diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).centerCrop().into(this)
    }
    visible()
}

@BindingAdapter("tools:cacheViewModel", "tools:progressView", "tools:cardCurrentImage", "tools:cardNextImage", requireAll = true)
fun ImageView.loadUserImage(viewModel: HomeViewModel, progressBar: ProgressBar, cardCurrentImage: String?, cardNextImage: String?) {
    progressBar.visible()
    if (cardCurrentImage == null) {
        progressBar.gone()
        Glide.with(context).load(R.color.colorTransparent).into(this)
    } else {
        val lruBitmap = viewModel.getBitmapFromCache(cardCurrentImage)
        if (lruBitmap != null) {
            progressBar.gone()
            Glide.with(context).load(lruBitmap).priority(Priority.IMMEDIATE)
                .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                .diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).centerCrop().into(this)
        } else {
            Glide.with(context).asBitmap().load(getGistPrefs().imagesURL + cardCurrentImage)
                .override(getKoinContext().resources.displayMetrics.widthPixels) // Keep original aspect ratio
                .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                .priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        progressBar.gone()

                        getGlide().load(resource).priority(Priority.IMMEDIATE)
                            .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                            .diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade()).centerCrop()
                            .into(this@loadUserImage)

                        viewModel.addBitmapToCache(cardCurrentImage, resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
        }

        if (cardNextImage != null) {
            val lruNextBitmap = viewModel.getBitmapFromCache(cardNextImage)

            if (lruNextBitmap == null) {
                Glide.with(context).asBitmap().load(getGistPrefs().imagesURL + cardNextImage)
                    .override(getKoinContext().resources.displayMetrics.widthPixels) // Keep original aspect ratio
                    .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                    .priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL).into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            viewModel.addBitmapToCache(cardNextImage, resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            }
        }
    }
}

@BindingAdapter("tools:profileImageUrl")
fun ImageView.loadProfileImage(imageUrl: String?) {
    if (imageUrl == null) return

    getGlide().load(getGistPrefs().imagesURL + imageUrl).priority(Priority.IMMEDIATE)
        .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._36sdp)) // Keep original aspect ratio
        .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
        .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().transition(DrawableTransitionOptions.withCrossFade()).into(this)
}

@BindingAdapter("tools:seenMessageProfile")
fun ImageView.loadSeenMessageProfile(imageUrl: String?) {
    if (imageUrl == null) return

    getGlide().load(imageUrl).priority(Priority.IMMEDIATE)
        .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)) // Keep original aspect ratio
        .placeholder(R.drawable.ic_user).downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
        .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().transition(DrawableTransitionOptions.withCrossFade()).into(this)
}

@BindingAdapter("tools:chatImage")
fun ImageView.loadChatImage(imageUrl: String?) {
    if (imageUrl == null) {
        getGlide().load(R.drawable.chat_place_holder).into(this)
        return
    }

    if (isNetworkPath(imageUrl)) {
        getGlide().load(imageUrl).centerCrop().priority(Priority.IMMEDIATE)
            .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
            .placeholder(R.drawable.chat_place_holder).diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    } else {
        getGlide().load(Uri.fromFile(File(imageUrl))).centerCrop().priority(Priority.IMMEDIATE)
            .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
            .placeholder(R.drawable.chat_place_holder).diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
}

@BindingAdapter("tools:chatProfileImageUrl")
fun ImageView.loadChatProfileImage(imageUrl: String?) {
    if (imageUrl == null) {
        getGlide().load(R.drawable.ic_user).priority(Priority.IMMEDIATE)
            .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._72sdp)) // Keep original aspect ratio
            .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
            .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().transition(DrawableTransitionOptions.withCrossFade()).into(this)
        return
    }

    getGlide().load(getGistPrefs().imagesURL + imageUrl).priority(Priority.IMMEDIATE)
        .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._72sdp)) // Keep original aspect ratio
        .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
        .placeholder(R.drawable.ic_user).thumbnail(
            getGlide().load(getGistPrefs().imagesURL + imageUrl).priority(Priority.IMMEDIATE)
                .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                .placeholder(R.drawable.ic_user).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade()).circleCrop()
                .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._36sdp)) // Adjust dynamically
        ).diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().transition(DrawableTransitionOptions.withCrossFade()).into(this)
}

@BindingAdapter("tools:notificationImage")
fun ImageView.loadNotificationImage(imageUrl: String?) {
    if (imageUrl == null) {
        getGlide().load(R.drawable.logo_belive).priority(Priority.IMMEDIATE)
            .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._72sdp)) // Keep original aspect ratio
            .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
            .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().transition(DrawableTransitionOptions.withCrossFade()).into(this)
        return
    }

    getGlide().load(getGistPrefs().imagesURL + imageUrl).priority(Priority.IMMEDIATE)
        .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._72sdp)) // Keep original aspect ratio
        .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
        .placeholder(R.drawable.ic_user).thumbnail(
            getGlide().load(getGistPrefs().imagesURL + imageUrl).priority(Priority.IMMEDIATE)
                .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                .placeholder(R.drawable.ic_user).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade()).circleCrop()
                .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._36sdp)) // Adjust dynamically
        ).diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().transition(DrawableTransitionOptions.withCrossFade()).into(this)
}

@BindingAdapter("tools:chatReplyImage")
fun ImageView.loadChatReplyImage(imageUrl: String?) {
    if (imageUrl == null) return

    getGlide().load(imageUrl).priority(Priority.IMMEDIATE).downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
        .placeholder(R.drawable.chat_place_holder).thumbnail(
            getGlide().load(imageUrl).priority(Priority.IMMEDIATE)
                .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                .placeholder(R.drawable.chat_place_holder).diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._36sdp)) // Adjust dynamically
        ).diskCacheStrategy(DiskCacheStrategy.ALL).transition(DrawableTransitionOptions.withCrossFade())
        .override(resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._72sdp)).into(this)
}

@BindingAdapter("tools:imageUrl", "tools:isIcon", "tools:isCircle", requireAll = false)
fun ImageView.loadImage(imageUrl: String?, isIcon: Boolean?, isCircle: Boolean = false) {
    if ((imageUrl == null) || (isIcon == null)) return

    post {
        val ratio = width.toFloat() / height.toFloat()

        if (isCircle) {
            getGlide().load(if (isIcon) getGistPrefs().iconURL else getGistPrefs().imagesURL + imageUrl).priority(Priority.IMMEDIATE)
                .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                .thumbnail(
                    getGlide().load(if (isIcon) getGistPrefs().iconURL else getGistPrefs().imagesURL + imageUrl).priority(Priority.IMMEDIATE)
                        .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                        .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).circleCrop()
                        .override(100, (100 / ratio).toInt()) // Adjust dynamically
                ).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).circleCrop().into(this)
        } else {
            getGlide().load(if (isIcon) getGistPrefs().iconURL else getGistPrefs().imagesURL + imageUrl).priority(Priority.IMMEDIATE)
                .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                .thumbnail(
                    getGlide().load(if (isIcon) getGistPrefs().iconURL else getGistPrefs().imagesURL + imageUrl).priority(Priority.IMMEDIATE)
                        .downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
                        .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().transition(DrawableTransitionOptions.withCrossFade())
                        .override(100, (100 / ratio).toInt()) // Adjust dynamically
                ).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).into(this)
        }
    }
}

@BindingAdapter("tools:customAdUrl")
fun ImageView.loadCustomAd(customAdUrl: String?) {
    if (customAdUrl == null) return

    // Create circular progress programmatically
    val parentView = parent as ViewGroup

    // Remove any existing progress bar first
    parentView.children.filterIsInstance<ProgressBar>().forEach { parentView.removeView(it) }

    var progressIndicator: ProgressBar?

    progressIndicator = ProgressBar(getKoinContext()).apply {
        id = View.generateViewId()
        elevation = 5f
        isIndeterminate = true
        indeterminateTintList = ColorStateList.valueOf(MaterialColors.getColor(parentView, R.attr.colorOnBackground90))
        layoutParams = when (parentView) {
            is ConstraintLayout -> ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
            )

            is FrameLayout -> FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            )

            is LinearLayout -> LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.CENTER
            }

            else -> ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    // Add progress indicator to parent
    parentView.addView(progressIndicator)

    elevation = 2f

    fun centerInConstraintLayout(parent: ConstraintLayout, view: View, matchView: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(parent)

        constraintSet.clear(view.id, ConstraintSet.LEFT)
        constraintSet.clear(view.id, ConstraintSet.TOP)
        constraintSet.clear(view.id, ConstraintSet.RIGHT)
        constraintSet.clear(view.id, ConstraintSet.BOTTOM)

        // Center horizontally
        constraintSet.connect(
            view.id, ConstraintSet.START, matchView.id, ConstraintSet.START, 0
        )
        constraintSet.connect(
            view.id, ConstraintSet.END, matchView.id, ConstraintSet.END, 0
        )

        // Center vertically
        constraintSet.connect(
            view.id, ConstraintSet.TOP, matchView.id, ConstraintSet.TOP, 0
        )
        constraintSet.connect(
            view.id, ConstraintSet.BOTTOM, matchView.id, ConstraintSet.BOTTOM, 0
        )

        // Apply the constraints
        constraintSet.applyTo(parent)
    }

    fun centerInFrameLayout(view: View) {
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = Gravity.CENTER
    }

    fun centerInLinearLayout(view: View) {
        val layoutParams = view.layoutParams as LinearLayout.LayoutParams
        layoutParams.gravity = Gravity.CENTER
    }

    fun assignMissingIdsRecursively(parent: ViewGroup) {
        parent.children.forEach { child ->
            if (child.id == View.NO_ID) {
                child.id = View.generateViewId()
            }

            if (child is ViewGroup) {
                assignMissingIdsRecursively(child) // Recurse into child ViewGroup
            }
        }
    }

    assignMissingIdsRecursively(parentView)

    when (parentView) {
        is ConstraintLayout -> centerInConstraintLayout(parentView, progressIndicator, this)
        is FrameLayout -> centerInFrameLayout(progressIndicator)
        is LinearLayout -> centerInLinearLayout(progressIndicator)
    }

    // Function to safely remove progress indicator
    fun removeProgress() {
        this@loadCustomAd.elevation = 0f
        progressIndicator?.elevation = -2f
        progressIndicator?.gone()

        try {
            if (progressIndicator?.parent != null) {
                parentView.removeView(progressIndicator)
            }
        } catch (e: Exception) {
            // Handle any potential exceptions during removal
        }
        progressIndicator = null
    }

    // Load image with Glide
    getGlide().asBitmap().downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
        .priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL)
        .load(customAdUrl).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                this@loadCustomAd.setImageBitmap(resource)
                removeProgress()
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                this@loadCustomAd.setImageDrawable(placeholder)
                removeProgress()
            }
        })
}

@BindingAdapter("tools:loadSelfie")
fun ImageView.loadSelfie(path: String?) {
    if (path == null) {
        getGlide().load(R.drawable.transparent).into(this)
    } else {
        getGlide().load(path).priority(Priority.IMMEDIATE).downsample(DownsampleStrategy.AT_LEAST).centerCrop().into(this)
    }
}

@BindingAdapter("tools:loadSelectedImage", "tools:overlaySelectedImage", "tools:isNetworkImage", requireAll = true)
fun ImageView.loadSelectedImage(path: String?, reject: Reject?, isNetworkImage: Boolean = false) {
    if (path.isNullOrEmpty()) {
        getGlide().load(R.drawable.transparent).into(this)
    } else {
        setBackgroundResource(R.color.boxBackground)

        // Create circular progress programmatically
        val parentView = parent as ViewGroup

        // Remove any existing progress bar first
        parentView.children.filterIsInstance<ProgressBar>().forEach { parentView.removeView(it) }

        var progressIndicator: ProgressBar?

        progressIndicator = ProgressBar(getKoinContext()).apply {
            id = View.generateViewId()
            elevation = 5f
            isIndeterminate = true
            indeterminateTintList = ColorStateList.valueOf(MaterialColors.getColor(parentView, R.attr.colorOnBackground90))
            layoutParams = when (parentView) {
                is ConstraintLayout -> ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                )

                is FrameLayout -> FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                )

                is LinearLayout -> LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    gravity = Gravity.CENTER
                }

                else -> ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
        }

        // Add progress indicator to parent
        parentView.addView(progressIndicator)

        elevation = 2f

        fun centerInConstraintLayout(parent: ConstraintLayout, view: View, matchView: View) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(parent)

            constraintSet.clear(view.id, ConstraintSet.LEFT)
            constraintSet.clear(view.id, ConstraintSet.TOP)
            constraintSet.clear(view.id, ConstraintSet.RIGHT)
            constraintSet.clear(view.id, ConstraintSet.BOTTOM)

            // Center horizontally
            constraintSet.connect(
                view.id, ConstraintSet.START, matchView.id, ConstraintSet.START, 0
            )
            constraintSet.connect(
                view.id, ConstraintSet.END, matchView.id, ConstraintSet.END, 0
            )

            // Center vertically
            constraintSet.connect(
                view.id, ConstraintSet.TOP, matchView.id, ConstraintSet.TOP, 0
            )
            constraintSet.connect(
                view.id, ConstraintSet.BOTTOM, matchView.id, ConstraintSet.BOTTOM, 0
            )

            // Apply the constraints
            constraintSet.applyTo(parent)
        }

        fun centerInFrameLayout(view: View) {
            val layoutParams = view.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.CENTER
        }

        fun centerInLinearLayout(view: View) {
            val layoutParams = view.layoutParams as LinearLayout.LayoutParams
            layoutParams.gravity = Gravity.CENTER
        }

        fun assignMissingIdsRecursively(parent: ViewGroup) {
            parent.children.forEach { child ->
                if (child.id == View.NO_ID) {
                    child.id = View.generateViewId()
                }

                if (child is ViewGroup) {
                    assignMissingIdsRecursively(child) // Recurse into child ViewGroup
                }
            }
        }

        assignMissingIdsRecursively(parentView)

        when (parentView) {
            is ConstraintLayout -> centerInConstraintLayout(parentView, progressIndicator, this)
            is FrameLayout -> centerInFrameLayout(progressIndicator)
            is LinearLayout -> centerInLinearLayout(progressIndicator)
        }

        // Function to safely remove progress indicator
        fun removeProgress() {
            setBackgroundResource(android.R.color.transparent)
            progressIndicator?.gone()
            progressIndicator?.elevation = -2f
            parentView.post {
                try {
                    if (progressIndicator?.parent != null) {
                        parentView.removeView(progressIndicator)
                    }
                } catch (e: Exception) {
                    // Handle any potential exceptions during removal
                }

                if (reject == null || reject == Reject.NOT_SAFE) {
                    foreground = null
                } else {
                    foreground = ColorDrawable().apply {
                        color = ContextCompat.getColor(context, R.color.black_50)
                    }
                }
                this.elevation = 0f
                progressIndicator = null
            }
        }

        // Load image with Glide
        getGlide().asBitmap().downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
            .priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL)
            .load(if (isNetworkImage) "${getGistPrefs().imagesURL}$path" else path).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    this@loadSelectedImage.setImageBitmap(resource)
                    removeProgress()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    this@loadSelectedImage.setImageDrawable(placeholder)
                    removeProgress()
                }
            })
    }
}

@BindingAdapter("tools:loadIcon")
fun ImageView.loadIcon(path: String?) {
    if (path == null) {
        getGlide().load(R.drawable.transparent).into(this)
    } else {
        // Dynamically create a ProgressBar
        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
            isVisible = true // Initially visible
            layoutParams = this@loadIcon.layoutParams // Use the same layout params as the ImageView
        }

        // Add the ProgressBar to the parent layout of the ImageView
        val parentLayout = parent as ViewGroup
        val index = parentLayout.indexOfChild(this)
        parentLayout.addView(progressBar, index) // Add the ProgressBar just above the ImageView

        isVisible = false

        // Load image with Glide
        getGlide().asBitmap().downsample(DownsampleStrategy.AT_LEAST) // Scale down if larger than the target size
            .placeholder(R.drawable.ic_error_circle).priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL)
            .load(getGistPrefs().iconURL + path).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    this@loadIcon.setImageBitmap(resource)
                    this@loadIcon.isVisible = true

                    // Hide the ProgressBar once the image is loaded
                    progressBar.isVisible = false
                    parentLayout.removeView(progressBar)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    this@loadIcon.setImageDrawable(placeholder)
                    this@loadIcon.isVisible = true

                    // Hide the ProgressBar once the image is loaded
                    progressBar.isVisible = false
                    parentLayout.removeView(progressBar)
                }
            })
    }
}

@BindingAdapter("tools:squircle_border_color", "tools:squircle_background_color", requireAll = false)
fun SquircleConstraintLayout.applyBorderColor(@ColorRes borderColor: Int? = null, @ColorRes backgroundColor: Int? = null) {
    if (borderColor != null) {
        style.borderColorRes = borderColor
    }
    if (backgroundColor != null) {
        style.backgroundColorRes = backgroundColor
    }
}

@BindingAdapter("tools:textColor")
fun TextView.textColor(@ColorRes color: Int) {
    try {
        setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, color)))
    } catch (e: Exception) {
        try {
            setTextColor(ContextCompat.getColor(context, color))
        } catch (e: Exception) {

        }
    }
}

@BindingAdapter("tools:tint")
fun ImageView.setImageTint(@ColorRes colorRes: Int) {
    ImageViewCompat.setImageTintList(
        this, ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
    )
}

@BindingAdapter("tools:translationXAnimation", "tools:isReverse", requireAll = true)
fun View.translationXAnimation(translationXAnimation: Float, isReverse: Boolean) {
    val animator = if (isReverse) {
        ObjectAnimator.ofFloat(this, "translationX", translationXAnimation, -translationXAnimation)
    } else {
        ObjectAnimator.ofFloat(this, "translationX", -translationXAnimation, translationXAnimation)
    }
    animator.duration = 1000 // Adjust speed
    animator.repeatMode = ValueAnimator.REVERSE
    animator.repeatCount = ValueAnimator.INFINITE
    animator.interpolator = LinearInterpolator()
    animator.start()
}

@BindingAdapter("tools:flashBackground")
fun View.flashBackground(isFlash: Boolean? = true) {
    if (isFlash == true) {
        post {
            val width = width // Use ImageView's width
            val height = height // Use ImageView's height
            val radius = width.coerceAtMost(height) / 2f // Use the smaller dimension to fit the circle

            if (width <= 0 || height <= 0) {
                logger("--flash--", "ImageView dimensions are invalid (width: $width, height: $height). Animation will not start.")
                return@post
            }

            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.isAntiAlias = true

            val colors = intArrayOf(
                "#FFFFFF".toColorInt(),
                "#7A96AC".toColorInt(),
                "#FFFFFF".toColorInt(),
                "#7A96AC".toColorInt(),
                "#FFFFFF".toColorInt(),
            )
            val positions = floatArrayOf(0f, 0.25f, 0.50f, 0.75f, 1f)
            val shader = SweepGradient(radius, radius, colors, positions)
            paint.shader = shader

            val animator = ValueAnimator.ofFloat(0f, 360f)
            animator.duration = 5000 // 5 seconds
            animator.repeatCount = ValueAnimator.INFINITE // Set to repeat infinitely
            animator.repeatMode = ValueAnimator.RESTART // Start from the beginning on each repeat
            animator.interpolator = LinearInterpolator()

            animator.addUpdateListener { animation ->
                val angle = animation.animatedValue as Float
                logger("--flash--", "angle: $angle")
                val matrix = Matrix()
                matrix.postRotate(angle, radius, radius)
                shader.setLocalMatrix(matrix)

                canvas.drawCircle(radius, radius, radius, paint)
                background = bitmap.toDrawable(resources) // Set bitmap as background
            }

            animator.start()
        }
    } else {
        background = null
        clearAnimation()
    }
}

@BindingAdapter("tools:unsafe")
fun BlurView.unsafe(reject: Reject?) {
    if (reject != null) {
        if (reject == Reject.NOT_SAFE) {
            isVisible = true

            setBlurEnabled(true)

            val activity = getKoinActivity()
            val decorView: View = activity.window.decorView
            val rootView: ViewGroup = decorView.findViewById(android.R.id.content)
            val windowBackground: Drawable? = decorView.background

            setupWith(rootView).setBlurAutoUpdate(true).setFrameClearDrawable(windowBackground).setBlurRadius(25f)
        } else {
            isVisible = false

            setBlurEnabled(false)
        }
    } else {
        isVisible = false

        setBlurEnabled(false)
    }
}

@BindingAdapter("tools:applyBlurRadius")
fun BlurView.applyBlur(blurRadius: Float) {
    post {
        try {
            val decorView: View = getKoinActivity().window.decorView
            val rootView: ViewGroup = decorView.findViewById(android.R.id.content)
            val windowBackground: Drawable? = decorView.background

            setupWith(rootView).setBlurAutoUpdate(true).setFrameClearDrawable(windowBackground).setBlurRadius(blurRadius)
        } catch (e: Exception) {
            catchLog("applyBlur: " + gsonString(e))
        }
    }
}

@BindingAdapter("tools:enable")
fun View.enable(isEnable: Boolean) {
    if (isEnable) {
        isEnabled = true
        isClickable = true
    } else {
        isEnabled = false
        isClickable = false
    }
}