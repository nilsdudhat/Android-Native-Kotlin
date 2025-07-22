package com.belive.dating.activities.edit_profile.basics.religion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.ReligionData
import com.belive.dating.databinding.ItemEditRegionBinding

class EditReligionAdapter(
    private var callBack: OnReligionChangeListener,
    private val viewModel: EditReligionViewModel,
) : RecyclerView.Adapter<EditReligionAdapter.ViewHolder>() {

    interface OnReligionChangeListener {
        fun onReligionClick(position: Int)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<ReligionData>() {
        override fun areItemsTheSame(oldItem: ReligionData, newItem: ReligionData): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name)
        }

        override fun areContentsTheSame(oldItem: ReligionData, newItem: ReligionData): Boolean {
            return oldItem == newItem
        }
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditRegionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.position = position
        holder.binding.name = asyncListDiffer.currentList[position].name
        holder.binding.isSelected = viewModel.selectedReligion.get() == asyncListDiffer.currentList[position].id
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callBack.onReligionClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class ViewHolder(val binding: ItemEditRegionBinding) :
        RecyclerView.ViewHolder(binding.root)
}