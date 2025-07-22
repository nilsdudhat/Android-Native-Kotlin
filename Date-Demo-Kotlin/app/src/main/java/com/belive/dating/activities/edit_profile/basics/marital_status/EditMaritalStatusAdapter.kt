package com.belive.dating.activities.edit_profile.basics.marital_status

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.MaritalStatusData
import com.belive.dating.databinding.ItemEditSexualOrientationBinding

class EditMaritalStatusAdapter(
    private var callBack: OnMaritalStatusChangeListener,
    private val viewModel: EditMaritalStatusViewModel,
) : RecyclerView.Adapter<EditMaritalStatusAdapter.ViewHolder>() {

    interface OnMaritalStatusChangeListener {
        fun onMaritalStatusClick(position: Int)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<MaritalStatusData>() {
        override fun areItemsTheSame(oldItem: MaritalStatusData, newItem: MaritalStatusData): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name)
        }

        override fun areContentsTheSame(oldItem: MaritalStatusData, newItem: MaritalStatusData): Boolean {
            return oldItem == newItem
        }
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditSexualOrientationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.position = position
        holder.binding.name = asyncListDiffer.currentList[position].name
        holder.binding.isSelected = viewModel.selectedMaritalStatus.get() == asyncListDiffer.currentList[position].id
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callBack.onMaritalStatusClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class ViewHolder(val binding: ItemEditSexualOrientationBinding) :
        RecyclerView.ViewHolder(binding.root)
}