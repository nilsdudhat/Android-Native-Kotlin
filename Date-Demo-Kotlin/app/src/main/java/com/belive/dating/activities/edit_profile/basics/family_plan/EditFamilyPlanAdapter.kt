package com.belive.dating.activities.edit_profile.basics.family_plan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.FamilyPlanData
import com.belive.dating.databinding.ItemEditFamilyPlanBinding

class EditFamilyPlanAdapter(
    private var callBack: OnFamilyPlanChangeListener,
    private val viewModel: EditFamilyPlanViewModel,
) : RecyclerView.Adapter<EditFamilyPlanAdapter.ViewHolder>() {

    interface OnFamilyPlanChangeListener {
        fun onFamilyPlanClick(position: Int)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<FamilyPlanData>() {
        override fun areItemsTheSame(oldItem: FamilyPlanData, newItem: FamilyPlanData): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name)
        }

        override fun areContentsTheSame(oldItem: FamilyPlanData, newItem: FamilyPlanData): Boolean {
            return oldItem == newItem
        }
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditFamilyPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.position = position
        holder.binding.name = asyncListDiffer.currentList[position].name
        holder.binding.isSelected = viewModel.selectedFamilyPlan.get() == asyncListDiffer.currentList[position].id
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callBack.onFamilyPlanClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class ViewHolder(val binding: ItemEditFamilyPlanBinding) : RecyclerView.ViewHolder(binding.root)
}