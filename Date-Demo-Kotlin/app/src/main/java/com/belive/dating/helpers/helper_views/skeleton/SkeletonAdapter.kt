package com.belive.dating.helpers.helper_views.skeleton

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SkeletonAdapter(
    private val itemLayoutResId: Int,
    private val itemCount: Int,
) : RecyclerView.Adapter<SkeletonAdapter.SkeletonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeletonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(itemLayoutResId, parent, false)
        return SkeletonViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkeletonViewHolder, position: Int) {
        // Apply skeleton effect while loading
        holder.itemView.rootView.showSkeleton(true)
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    class SkeletonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}