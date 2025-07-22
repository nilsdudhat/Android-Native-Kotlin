package com.belive.dating.activities.dashboard.fragments.ls.fragments.super_like

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.user.models.liked_me_profiles.LikedMeProfile
import com.belive.dating.databinding.ItemLikeBinding
import com.belive.dating.extensions.formatStringsForWidth
import com.belive.dating.extensions.getMaxAvailableWidth
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.visible
import kotlin.math.roundToInt

class SuperLikeAdapter(
    private val viewModel: SuperLikeViewModel,
    private val likeItemClickListener: LikeItemClickListener,
    private val lastItemListener: LastItemListener,
) :
    RecyclerView.Adapter<SuperLikeAdapter.ViewHolder>() {

    interface LastItemListener {
        fun onLastItemListener()
    }

    interface LikeItemClickListener {
        fun onLikeItemClick(holder: ViewHolder)
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        if (holder.bindingAdapterPosition == viewModel.superLikedMeProfileList.value!!.size - 1) {
            lastItemListener.onLastItemListener()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLikeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = viewModel.superLikedMeProfileList.value!![position]
        holder.binding.likeProfile = profile
        holder.binding.progress.visible()

        holder.binding.isBlur = !getUserPrefs().isAiMatchMaker

        holder.binding.main.setOnClickListener {
            likeItemClickListener.onLikeItemClick(holder)
        }

        holder.binding.distance = getDistance(profile)

        val name = profile.fullName
        val age = StringBuilder().append("• ").append(profile.age).toString()

        holder.binding.txtName.getMaxAvailableWidth {
            formatStringsForWidth(name.trim(), age.trim(), holder.binding.txtName, it) { spannableString ->
                holder.binding.name = spannableString
            }
        }
    }

    private fun getDistance(profile: LikedMeProfile): String {
        val distance = profile.distance.roundToInt()

        val strDistance: String = if (getUserPrefs().countryCode.equals("IN", true)) {
            if (distance == 0) {
                StringBuilder().append("1 km away").toString()
            } else {
                StringBuilder().append(distance).append(" km away").toString()
            }
        } else {
            if (distance == 0) {
                StringBuilder().append("miles away").toString()
            } else {
                StringBuilder().append(distance).append(" miles away").toString()
            }
        }

        return strDistance
    }

    override fun getItemCount(): Int {
        return viewModel.superLikedMeProfileList.value?.size ?: 10
    }

    class ViewHolder(val binding: ItemLikeBinding) : RecyclerView.ViewHolder(binding.root)
}