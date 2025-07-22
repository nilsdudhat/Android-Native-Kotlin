package com.belive.dating.activities.dashboard.fragments.home

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.text.style.LocaleSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.belive.dating.R
import com.belive.dating.activities.dashboard.InterestProfileAdapter
import com.belive.dating.ads.BigNativeGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.api.user.models.home_profiles.User
import com.belive.dating.databinding.FragmentStoryBinding
import com.belive.dating.databinding.ItemHomeCardAdBinding
import com.belive.dating.databinding.ItemUserCardBinding
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.formatStringsForWidth
import com.belive.dating.extensions.getAdsPrefs
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getMaxAvailableWidth
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gone
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.extensions.throttleFirstClick
import com.belive.dating.extensions.visible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

class UserCardAdapter(
    val activity: FragmentActivity,
    val viewModel: HomeViewModel,
    val userCardClickListener: UserCardClickListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface UserCardClickListener {
        fun onUserDetailsClick(userId: Int)
        fun onMessageClick(userId: Int)
    }

    private val VIEW_TYPE_CONTENT = 0
    private val VIEW_TYPE_AD = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                val binding = ItemHomeCardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            else -> {
                val binding = ItemUserCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ContentViewHolder(activity, binding)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_AD -> {
                bindAd(viewHolder as AdViewHolder)
            }

            VIEW_TYPE_CONTENT -> {
                bindContent(viewHolder as ContentViewHolder, userCardClickListener)
            }
        }
    }

    fun bindAd(holder: AdViewHolder) {
        ManageAds.showNativeFullScreenAd(BigNativeGroup.Main, holder.binding.frameAd)
    }

    fun bindContent(holder: ContentViewHolder, userCardClickListener: UserCardClickListener) {
        try {
            logger("--profile_card---", "bindingAdapterPosition: ${holder.bindingAdapterPosition}")
            logger("--profile_card---", "contentPosition: ${getContentPosition(holder.bindingAdapterPosition)}")

            val user = viewModel.homeUserList.get()?.get(getContentPosition(holder.bindingAdapterPosition))

            holder.binding.user = user

            holder.binding.viewPager.isUserInputEnabled = false

            viewModel.viewModelScope.launch {
                viewModel.dragState.collectLatest { pair ->
                    if (pair.first == holder.bindingAdapterPosition) {
                        holder.binding.direction = pair.second
                    } else {
                        holder.binding.direction = null
                    }
                }
            }

            holder.binding.viewPager.adapter = user?.userImages?.size.let {
                it?.let { it1 -> StoryPagerAdapter(activity, it1) }
            }

            if ((holder.binding.viewPager.adapter != null) && (holder.binding.viewPager.adapter!!.itemCount > 1)) {
                holder.binding.tabIndicator.visibility = View.VISIBLE
                TabLayoutMediator(
                    holder.binding.tabIndicator,
                    holder.binding.viewPager,
                ) { _, _ ->
                }.attach()

                for (i in 0 until holder.binding.tabIndicator.tabCount) {
                    val tab = (holder.binding.tabIndicator.getChildAt(0) as ViewGroup).getChildAt(i)
                    val p = tab.layoutParams as MarginLayoutParams
                    p.setMargins(
                        activity.getDimensionPixelOffset(com.intuit.sdp.R.dimen._2sdp),
                        0,
                        activity.getDimensionPixelOffset(com.intuit.sdp.R.dimen._2sdp),
                        0,
                    )
                    tab.requestLayout()
                }
            } else {
                holder.binding.tabIndicator.visibility = View.GONE
            }

            val layoutManager = FlexboxLayoutManager(holder.binding.root.context)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.FLEX_START

            holder.binding.rvInterests.layoutManager = layoutManager

            val adapter = user?.interests?.let { InterestProfileAdapter(it) }
            holder.binding.rvInterests.adapter = adapter

            val name = user?.fullName
            val age = StringBuilder().append("• ").append(user?.age).toString()

            if (name != null) {
                holder.binding.txtName.getMaxAvailableWidth {
                    formatStringsForWidth(name.trim(), age.trim(), holder.binding.txtName, it) { spannableString ->
                        holder.binding.name = spannableString
                    }
                }
            }

            val (strLocation: String, strDistance: String) = getLocationData(holder, user)

            holder.binding.txtLocation.getMaxAvailableWidth {
                formatLocation(strLocation, strDistance, holder.binding.txtLocation, it) { spannableString ->
                    holder.binding.location = spannableString
                }
            }

            holder.binding.viewPager.currentItem = 0

            holder.binding.userImage = null
            holder.binding.progress.visible()
            holder.binding.executePendingBindings()

            holder.binding.userImage = user?.userImages?.get(holder.binding.viewPager.currentItem)?.image

            holder.binding.backward.setOnClickListener {
                if (0 < holder.binding.viewPager.currentItem) {
                    holder.binding.viewPager.currentItem -= 1

                    holder.binding.userImage = viewModel.homeUserList.get()?.get(getContentPosition(holder.bindingAdapterPosition))?.userImages?.get(
                        holder.binding.viewPager.currentItem
                    )?.image
                }
            }

            holder.binding.forward.setOnClickListener {
                if ((viewModel.homeUserList.get()
                        ?.get(getContentPosition(holder.bindingAdapterPosition))?.userImages != null) && (holder.binding.viewPager.currentItem < viewModel.homeUserList.get()
                        ?.get(getContentPosition(holder.bindingAdapterPosition))?.userImages!!.size - 1)
                ) {
                    holder.binding.viewPager.currentItem += 1

                    holder.binding.userImage = viewModel.homeUserList.get()?.get(getContentPosition(holder.bindingAdapterPosition))?.userImages?.get(
                        holder.binding.viewPager.currentItem
                    )?.image
                }
            }

            holder.binding.btnMessage.safeClickListener(holder.bindingAdapterPosition) {
                userCardClickListener.onMessageClick(viewModel.homeUserList.get()?.get(getContentPosition(holder.bindingAdapterPosition))!!.id)
            }

            holder.binding.imgDetails.safeClickListener(holder.bindingAdapterPosition) {
                userCardClickListener.onUserDetailsClick(viewModel.homeUserList.get()?.get(getContentPosition(holder.bindingAdapterPosition))!!.id)
            }

            holder.binding.layoutDetails.safeClickListener(holder.bindingAdapterPosition) {
                userCardClickListener.onUserDetailsClick(viewModel.homeUserList.get()?.get(getContentPosition(holder.bindingAdapterPosition))!!.id)
            }
        } catch (e: Exception) {
            catchLog("CardStackAdapter: ${gsonString(e)}}")
        }
    }

    private fun formatLocation(
        startString: String,
        endString: String,
        textView: TextView,
        maxWidth: Int,
        onSpannableAvailable: (SpannableString) -> Unit,
    ) {
        val homeIcon = ContextCompat.getDrawable(activity, R.drawable.ic_card_location) // Custom home icon
        val locationIcon = ContextCompat.getDrawable(activity, R.drawable.ic_location) // Custom location pin

        // Scale icons
        homeIcon?.setBounds(
            0, 0, activity.getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp), activity.getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
        )
        locationIcon?.setBounds(
            0, 0, activity.getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp), activity.getDimensionPixelOffset(com.intuit.sdp.R.dimen._16sdp)
        )

        // Combine location and distance
        val fullText = "   ${startString.trim()}      ${endString.trim()}"

        // Create a SpannableString
        val spannable = SpannableString(fullText)

        // Apply bold style to the location
        spannable.setSpan(StyleSpan(Typeface.BOLD), 3, startString.length + 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            spannable.setSpan(ImageSpan(homeIcon!!, DynamicDrawableSpan.ALIGN_CENTER), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        } else {
            spannable.setSpan(ImageSpan(homeIcon!!, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            spannable.setSpan(
                ImageSpan(locationIcon!!, DynamicDrawableSpan.ALIGN_CENTER),
                startString.length + 6,
                startString.length + 7,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE,
            )
        } else {
            spannable.setSpan(
                ImageSpan(locationIcon!!, DynamicDrawableSpan.ALIGN_BASELINE),
                startString.length + 6,
                startString.length + 7,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE,
            )
        }

        // Set the ellipsis if the text exceeds maxWidth
        textView.post {
            // Measure the full text width
            val paint = textView.paint
            val fullTextWidth = paint.measureText(fullText)

            textView.gravity = Gravity.CENTER_VERTICAL

            // Check if it exceeds the max width
            if (fullTextWidth > maxWidth) {
                // Find the max length of location that fits within maxWidth - endString width
                val distanceWidth = paint.measureText(" $endString")
                val availableWidth = maxWidth - distanceWidth

                // Truncate the location and append ellipsis
                val truncatedLocation = TextUtils.ellipsize(
                    startString, paint, availableWidth, TextUtils.TruncateAt.END
                ).toString()

                // Update the spannable with truncated location
                val newFullText = "${truncatedLocation.trim()} ${endString.trim()}"
                val updatedSpannable = SpannableString(newFullText.trim())

                // Force LTR layout direction
                updatedSpannable.setSpan(LocaleSpan(Locale.ENGLISH), 0, updatedSpannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                onSpannableAvailable.invoke(updatedSpannable)
            } else {
                // Force LTR layout direction
                spannable.setSpan(LocaleSpan(Locale.ENGLISH), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                onSpannableAvailable.invoke(spannable)
            }
        }
    }

    private fun getLocationData(holder: ContentViewHolder, user: User?): Pair<String, String> {
        val strLocation: String
        val strDistance: String

        if (user?.isFake == true) {
            strLocation = if (!getUserPrefs().currentCity.isNullOrEmpty()) {
                getUserPrefs().currentCity!!
            } else if (!getUserPrefs().currentState.isNullOrEmpty()) {
                getUserPrefs().currentState!!
            } else if (!getUserPrefs().currentCountry.isNullOrEmpty()) {
                getUserPrefs().currentCountry!!
            } else {
                ""
            }

            if (strLocation.isEmpty()) {
                holder.binding.txtLocation.gone()
            } else {
                holder.binding.txtLocation.visible()
            }

            strDistance =
                StringBuilder().append("${(11..50).random()} ${if (getUserPrefs().countryCode.equals("IN", true)) "km" else "miles"}")
                    .append(" away")
                    .toString()
        } else {
            if (user?.city?.isNotEmpty() == true) {
                holder.binding.txtLocation.text = user.city
            } else if (user?.state?.isNotEmpty() == true) {
                holder.binding.txtLocation.text = user.state
            } else if (user?.country?.isNotEmpty() == true) {
                holder.binding.txtLocation.text = user.country
            } else {
                holder.binding.txtLocation.gone()
            }

            strLocation = StringBuilder().append(holder.binding.txtLocation.text).toString()

            val distance = user?.distance?.roundToInt()

            strDistance = if (getUserPrefs().countryCode == "IN") {
                if (distance == 0) {
                    StringBuilder().append("${(11..50).random()} km away").toString()
                } else {
                    StringBuilder().append(distance).append(" km away").toString()
                }
            } else {
                if (distance == 0) {
                    StringBuilder().append("${(11..50).random()} miles away").toString()
                } else {
                    StringBuilder().append(distance).append(" miles away").toString()
                }
            }
        }
        return Pair(strLocation, strDistance)
    }

    fun View.safeClickListener(position: Int, onClick: (View) -> Unit) {
        throttleFirstClick {
            if (position == viewModel.topPosition.value) {
                onClick(it)
            } else {
                // Do nothing, prevent click on non-top cards
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!ManageAds.isHomeCardAdsEnabled()) {
            VIEW_TYPE_CONTENT
        } else {
            if ((position + 1) % getAdsPrefs().swipeCardsAdInterval == 0) {
                VIEW_TYPE_AD
            } else {
                VIEW_TYPE_CONTENT
            }
        }
    }

    override fun getItemCount(): Int {
        return if (viewModel.isDataLoaded.get() == false) {
            1
        } else if (viewModel.homeUserList.get().isNullOrEmpty()) {
            0
        } else {
            viewModel.homeUserList.get()!!.size + getAdsCount()
        }
    }

    fun isAdPosition(position: Int): Boolean {
        return if (!ManageAds.isHomeCardAdsEnabled()) {
            false
        } else {
            (position > 0) && ((position % getAdsPrefs().swipeCardsAdInterval) == 0)
        }
    }

    fun getAdapterPosition(contentPosition: Int): Int {
        if (!ManageAds.isHomeCardAdsEnabled()) {
            return contentPosition
        } else {
            var adapterPos = contentPosition
            // Keep shifting until contentPosition == adapterPos - (adsBefore)
            while (adapterPos - (adapterPos / getAdsPrefs().swipeCardsAdInterval) < contentPosition) {
                adapterPos++
            }
            return adapterPos
        }
    }

    fun getContentPosition(adapterPosition: Int): Int {
        return if (!ManageAds.isHomeCardAdsEnabled()) {
            adapterPosition
        } else {
            adapterPosition - (adapterPosition / getAdsPrefs().swipeCardsAdInterval)
        }
    }

    private fun getAdsCount(): Int {
        return if (viewModel.isDataLoaded.get() == false) {
            0
        } else if (viewModel.homeUserList.get().isNullOrEmpty()) {
            0
        } else {
            return if (!ManageAds.isHomeCardAdsEnabled()) {
                0
            } else {
                (viewModel.homeUserList.get()!!.size / getAdsPrefs().swipeCardsAdInterval).toInt()
            }
        }
    }

    class AdViewHolder(val binding: ItemHomeCardAdBinding) : RecyclerView.ViewHolder(binding.root)

    class ContentViewHolder(val activity: FragmentActivity, val binding: ItemUserCardBinding) : RecyclerView.ViewHolder(binding.root)

    class StoryPagerAdapter(activity: FragmentActivity, private val numItems: Int = 10) : FragmentStateAdapter(activity) {

        override fun getItemCount() = numItems

        override fun createFragment(position: Int) = StoryPagerFragment()
    }

    class StoryPagerFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            val binding = FragmentStoryBinding.inflate(inflater)
            return binding.root
        }
    }
}