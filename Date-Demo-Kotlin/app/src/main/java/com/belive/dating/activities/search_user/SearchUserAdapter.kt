package com.belive.dating.activities.search_user

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.user.models.search_user.User
import com.belive.dating.databinding.ItemSearchUserBinding

class SearchUserAdapter(private val onItemClick: OnItemClickListener) :
    RecyclerView.Adapter<SearchUserAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(profileId: Int)
    }

    var userList = arrayListOf<User>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemSearchUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = userList[position]
        holder.binding.card = card

        holder.binding.executePendingBindings()

        holder.binding.root.setOnClickListener {
            onItemClick.onItemClick(card.id)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class ViewHolder(val binding: ItemSearchUserBinding) : RecyclerView.ViewHolder(binding.root)
}