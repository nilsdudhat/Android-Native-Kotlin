package com.demo.gallery.app.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.gallery.app.databinding.ItemMediaBinding
import com.demo.gallery.app.room.Media

class MediaAdapter() : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    private var mediaList = ArrayList<Media>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val media = mediaList[position]
        holder.binding.media = media
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMediaList(list: java.util.ArrayList<Media>) {
        mediaList = ArrayList(list)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root)
}