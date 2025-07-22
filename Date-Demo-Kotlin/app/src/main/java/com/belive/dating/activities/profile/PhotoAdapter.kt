package com.belive.dating.activities.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemProfilePhotoBinding
import com.belive.dating.extensions.getDimensionPixelOffset
import com.belive.dating.extensions.getScreenWidth

class PhotoAdapter(val viewModel: ProfileViewModel, val callback: OnPhotoClickListener) :
    RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    interface OnPhotoClickListener {
        fun onPhotoClick()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfilePhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = viewModel.photoList.get()?.get(position)
        holder.binding.photo = photo

        holder.binding.main.layoutParams.apply {
            width = ((getScreenWidth() - holder.itemView.context.getDimensionPixelOffset(com.intuit.sdp.R.dimen._8sdp)) / 2.5).toInt()
        }

        holder.binding.main.setOnClickListener {
            callback.onPhotoClick()
        }
    }

    override fun getItemCount(): Int {
        return viewModel.photoList.get()?.size ?: 0
    }

    class ViewHolder(val binding: ItemProfilePhotoBinding) : RecyclerView.ViewHolder(binding.root)
}