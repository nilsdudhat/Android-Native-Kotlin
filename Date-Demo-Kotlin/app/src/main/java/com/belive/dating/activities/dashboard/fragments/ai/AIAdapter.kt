package com.belive.dating.activities.dashboard.fragments.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.ads.BigNativeGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.ItemAdPlaceholderBinding
import com.belive.dating.databinding.ItemAiBinding
import com.belive.dating.extensions.formatStringsForWidth
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.getMaxAvailableWidth
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.visible

class AIAdapter(
    private val viewModel: AIViewModel,
    private val aiItemClickListener: AIItemClickListener,
    private val lastItemListener: LastItemListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface LastItemListener {
        fun onLastItemListener()
    }

    interface AIItemClickListener {
        fun onAIItemClick(holder: ContentViewHolder)
    }

    private val VIEW_TYPE_CONTENT = 0
    private val VIEW_TYPE_AD = 1
    private val AD_INTERVAL = 6

    val gridLayoutManager: GridLayoutManager by lazy {
        GridLayoutManager(getKoinContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == VIEW_TYPE_AD) spanCount else 1
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)

        if (holder.bindingAdapterPosition == itemCount - 1) {
            lastItemListener.onLastItemListener()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> AdViewHolder(ItemAdPlaceholderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ContentViewHolder(ItemAiBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ContentViewHolder -> bindContent(holder, position)
            is AdViewHolder -> bindAd(holder)
        }
    }

    private fun bindContent(holder: ContentViewHolder, adapterPosition: Int) {
        val contentPosition = getContentPosition(adapterPosition)
        val profile = viewModel.aiProfileList.value?.get(contentPosition) ?: return

        holder.binding.aiProfile = profile
        holder.binding.progress.visible()

        holder.binding.isBlur = when {
            viewModel.isAIMatchAvailable.get() == true -> false
            else -> !(getUserPrefs().aiRemainProfileCount != 0 && contentPosition < getUserPrefs().aiRemainProfileCount)
        }

        holder.binding.main.setOnClickListener {
            aiItemClickListener.onAIItemClick(holder)
        }

        holder.binding.txtName.getMaxAvailableWidth { availableWidth ->
            formatStringsForWidth(
                profile.fullName.trim(),
                "• ${profile.age}".trim(),
                holder.binding.txtName,
                availableWidth
            ) { spannableString ->
                holder.binding.name = spannableString
            }
        }
    }

    private fun bindAd(holder: AdViewHolder) {
        ManageAds.showNativeInGridAd(BigNativeGroup.Main, holder.binding.adPlaceholder)

        (holder.binding.adPlaceholder.layoutParams as GridLayoutManager.LayoutParams).bottomMargin = getKoinContext().getDimensionPixelOffset(com.intuit.sdp.R.dimen._8sdp)
        (holder.binding.adPlaceholder.layoutParams as GridLayoutManager.LayoutParams).marginStart = getKoinContext().getDimensionPixelOffset(com.intuit.sdp.R.dimen._4sdp)
        (holder.binding.adPlaceholder.layoutParams as GridLayoutManager.LayoutParams).marginEnd = getKoinContext().getDimensionPixelOffset(com.intuit.sdp.R.dimen._4sdp)
    }

    override fun getItemCount(): Int {
        return viewModel.aiProfileList.value?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAdPosition(position)) VIEW_TYPE_AD else VIEW_TYPE_CONTENT
    }

    private fun isAdPosition(position: Int): Boolean {
        return (position + 1) % (AD_INTERVAL + 1) == 0
    }

    fun getContentPosition(adapterPosition: Int): Int {
        return adapterPosition - (adapterPosition + 1) / (AD_INTERVAL + 1)
    }

    class ContentViewHolder(val binding: ItemAiBinding) : RecyclerView.ViewHolder(binding.root)

    class AdViewHolder(val binding: ItemAdPlaceholderBinding) : RecyclerView.ViewHolder(binding.root)
}