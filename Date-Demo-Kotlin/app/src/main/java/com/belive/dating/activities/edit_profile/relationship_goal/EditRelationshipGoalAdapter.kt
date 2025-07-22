package com.belive.dating.activities.edit_profile.relationship_goal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.RelationshipGoalData
import com.belive.dating.databinding.ItemEditRelationshipGoalBinding

/**
 * Adapter for the RecyclerView displaying relationship goals in the edit screen.
 *
 * This adapter manages a list of [RelationshipGoalData] objects and handles user interactions
 * to select a relationship goal.  It uses DiffUtil for efficient updates to the list.
 *
 * @property callback An [OnRelationshipGoalListener] to handle click events on relationship goal items.
 * @property viewModel The [EditRelationshipGoalViewModel] that provides data and manages the selected relationship goal.
 */
class EditRelationshipGoalAdapter(
    private val callback: OnRelationshipGoalListener,
    val viewModel: EditRelationshipGoalViewModel,
) :
    RecyclerView.Adapter<EditRelationshipGoalAdapter.ViewHolder>() {

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
        val binding = ItemEditRelationshipGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.position = position
        holder.binding.name = asyncListDiffer.currentList[position].name
        holder.binding.isSelected = asyncListDiffer.currentList[position].id == viewModel.selectedRelationshipGoal.get()
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callback.onRelationshipGoalClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class ViewHolder(val binding: ItemEditRelationshipGoalBinding) : RecyclerView.ViewHolder(binding.root)
}