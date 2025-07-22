package com.belive.dating.activities.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.user.models.home_profiles.Interest
import com.belive.dating.databinding.ItemInterestProfileBinding
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexboxLayoutManager

class InterestProfileAdapter(private val interestList: List<Interest>) : RecyclerView.Adapter<InterestProfileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemInterestProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lp: ViewGroup.LayoutParams = holder.binding.txtInterest.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.flexGrow = 1.0f
            lp.alignSelf = AlignSelf.AUTO
        }

        holder.binding.interest = interestList[position].name
    }

    override fun getItemCount(): Int {
        return interestList.size
    }

    class ViewHolder(val binding: ItemInterestProfileBinding) : RecyclerView.ViewHolder(binding.root)
}