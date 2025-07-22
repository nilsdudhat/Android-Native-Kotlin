package com.belive.dating.activities.introduction.relationship_goal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.RelationshipGoalData
import com.belive.dating.databinding.ItemRelationshipGoalBinding

class RelationshipGoalAdapter(
    private val callback: OnRelationshipGoalListener,
    val viewModel: RelationshipGoalViewModel,
) :
    RecyclerView.Adapter<RelationshipGoalAdapter.ViewHolder>() {

    interface OnRelationshipGoalListener {
        fun onRelationshipGoalClick(position: Int)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<RelationshipGoalData>() {
        override fun areItemsTheSame(oldItem: RelationshipGoalData, newItem: RelationshipGoalData): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name) && (oldItem.icon == newItem.icon)
        }

        override fun areContentsTheSame(oldItem: RelationshipGoalData, newItem: RelationshipGoalData): Boolean {
            return oldItem == newItem
        }
    }

    var asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRelationshipGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.relationshipGoal = asyncListDiffer.currentList[position].name
        holder.binding.isSelected = viewModel.selectedRelationshipGoal.get() == asyncListDiffer.currentList[position].id
        holder.binding.executePendingBindings()

        holder.binding.button.setOnClickListener {
            callback.onRelationshipGoalClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class ViewHolder(val binding: ItemRelationshipGoalBinding) : RecyclerView.ViewHolder(binding.root)
}