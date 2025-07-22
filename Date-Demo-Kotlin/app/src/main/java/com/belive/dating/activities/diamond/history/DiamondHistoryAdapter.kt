package com.belive.dating.activities.diamond.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.ads.BannerGroup
import com.belive.dating.ads.ManageAds
import com.belive.dating.databinding.ItemAdPlaceholderBinding
import com.belive.dating.databinding.ItemDiamondHistoryBinding
import com.belive.dating.extensions.getAdsPrefs

class DiamondHistoryAdapter(
    val activity: FragmentActivity,
    val viewModel: DiamondHistoryViewModel,
    val lastItemCallback: OnDiamondLastItemListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnDiamondLastItemListener {
        fun onDiamondLastItem()
    }

    private val VIEW_TYPE_CONTENT = 0
    private val VIEW_TYPE_AD = 1
    private val AD_INTERVAL = 2

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)

        val contentSize = viewModel.diamondHistoryList.get()?.size ?: 0
        val adsCount = getAdsCount()
        val lastPosition = contentSize + adsCount - 1

        if (holder.bindingAdapterPosition == lastPosition) {
            lastItemCallback.onDiamondLastItem()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            VIEW_TYPE_AD -> AdViewHolder(ItemAdPlaceholderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ContentViewHolder(ItemDiamondHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is AdViewHolder -> bindAd(viewHolder)
            is ContentViewHolder -> bindContent(viewHolder, position)
        }
    }

    private fun bindContent(holder: ContentViewHolder, adapterPosition: Int) {
        val contentList = viewModel.diamondHistoryList.get() ?: return
        val contentPosition = getContentPosition(adapterPosition)

        if (contentPosition !in contentList.indices) return

        val transaction = contentList[contentPosition]
        holder.binding.divider.isVisible = contentPosition != contentList.lastIndex

        val (symbol, color) = when (transaction.txnType) {
            "purchase", "gift", "claimed" -> "+" to R.color.positive_color
            "withdrawal", "expired" -> "-" to R.color.negative_color
            else -> "" to 0
        }

        holder.binding.amountSymbol = symbol
        holder.binding.amountColor = color
        holder.binding.diamondTransaction = transaction
        holder.binding.executePendingBindings()
    }

    private fun bindAd(holder: AdViewHolder) {
        ManageAds.showBannerAd(BannerGroup.Diamond, holder.binding.adPlaceholder)
    }

    override fun getItemViewType(position: Int): Int {
        return if (shouldShowAd(position)) VIEW_TYPE_AD else VIEW_TYPE_CONTENT
    }

    override fun getItemCount(): Int {
        val contentSize = viewModel.diamondHistoryList.get()?.size ?: 0
        return contentSize + getAdsCount()
    }

    private fun getAdsCount(): Int {
        val contentSize = viewModel.diamondHistoryList.get()?.size ?: 0
        return if (shouldShowAds()) contentSize / AD_INTERVAL else 0
    }

    private fun getContentPosition(adapterPosition: Int): Int {
        return if (shouldShowAds()) {
            adapterPosition - (adapterPosition + 1) / (AD_INTERVAL + 1)
        } else {
            adapterPosition
        }
    }

    private fun shouldShowAd(position: Int): Boolean {
        return shouldShowAds() && (position + 1) % (AD_INTERVAL + 1) == 0
    }

    private fun shouldShowAds(): Boolean {
        return ManageAds.isBannerAdsEnabled() && getAdsPrefs().isGroupDiamondBanner
    }

    class AdViewHolder(val binding: ItemAdPlaceholderBinding) : RecyclerView.ViewHolder(binding.root)

    class ContentViewHolder(val binding: ItemDiamondHistoryBinding) : RecyclerView.ViewHolder(binding.root)
}