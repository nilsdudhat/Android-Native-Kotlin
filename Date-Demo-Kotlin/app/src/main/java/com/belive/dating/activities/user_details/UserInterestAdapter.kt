package com.belive.dating.activities.user_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemUserInterestBinding
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexboxLayoutManager

class UserInterestAdapter(val viewModel: UserDetailsViewModel) : RecyclerView.Adapter<UserInterestAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserInterestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lp: ViewGroup.LayoutParams = holder.binding.main.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.flexGrow = 1.0f
            lp.alignSelf = AlignSelf.AUTO
        }

        holder.binding.interestData = viewModel.userDetails.get()?.interests?.get(position)
    }

    override fun getItemCount(): Int {
        return viewModel.userDetails.get()?.interests?.size ?: 0
    }

    class ViewHolder(val binding: ItemUserInterestBinding) : RecyclerView.ViewHolder(binding.root)
}