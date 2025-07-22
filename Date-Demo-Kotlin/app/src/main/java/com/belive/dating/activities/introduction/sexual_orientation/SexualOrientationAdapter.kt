package com.belive.dating.activities.introduction.sexual_orientation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.Orientation
import com.belive.dating.databinding.ItemSexualOrientationBinding

class SexualOrientationAdapter(
    private var callBack: OnOrientationChangeListener,
    private val viewModel: SexualOrientationViewModel,
) : RecyclerView.Adapter<SexualOrientationAdapter.SexualHolder>() {

    interface OnOrientationChangeListener {
        fun onOrientationClick(position: Int)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Orientation>() {
        override fun areItemsTheSame(oldItem: Orientation, newItem: Orientation): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name)
        }

        override fun areContentsTheSame(oldItem: Orientation, newItem: Orientation): Boolean {
            return oldItem == newItem
        }
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SexualHolder {
        val binding =
            ItemSexualOrientationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SexualHolder(binding)
    }

    override fun onBindViewHolder(holder: SexualHolder, position: Int) {
        holder.binding.name = asyncListDiffer.currentList[position].name
        holder.binding.isSelected = viewModel.selectedSexuality.get() == asyncListDiffer.currentList[position].id
        holder.binding.executePendingBindings()

        holder.binding.button.setOnClickListener {
            callBack.onOrientationClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class SexualHolder(val binding: ItemSexualOrientationBinding) :
        RecyclerView.ViewHolder(binding.root)
}