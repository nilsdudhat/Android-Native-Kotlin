package com.belive.dating.activities.edit_profile.sexual_orientation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.Orientation
import com.belive.dating.databinding.ItemEditSexualOrientationBinding

/**
 *  RecyclerView Adapter for displaying and handling user selection of sexual orientations.
 *
 *  This adapter utilizes `AsyncListDiffer` for efficient list updates and `DiffUtil` for calculating
 *  minimal updates between lists.  It displays a list of `Orientation` objects, allowing the user to
 *  select one. The selected orientation is highlighted, and a callback is triggered on selection.
 *
 *  @property callBack  An [OnOrientationChangeListener] interface implementation to notify about orientation clicks.
 *  @property viewModel The [EditSexualOrientationViewModel] associated with this adapter, responsible for managing
 *                     the selected orientation state.
 */
class EditSexualOrientationAdapter(
    private var callBack: OnOrientationChangeListener,
    private val viewModel: EditSexualOrientationViewModel,
) : RecyclerView.Adapter<EditSexualOrientationAdapter.SexualHolder>() {

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
            ItemEditSexualOrientationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SexualHolder(binding)
    }

    override fun onBindViewHolder(holder: SexualHolder, position: Int) {
        holder.binding.position = position
        holder.binding.name = asyncListDiffer.currentList[position].name
        holder.binding.isSelected = viewModel.selectedSexualOrientation.get() == position
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            val previousPosition = viewModel.selectedSexualOrientation.get()
            viewModel.selectedSexualOrientation.set(holder.bindingAdapterPosition)
            if (previousPosition != null) {
                notifyItemChanged(previousPosition)
            }
            notifyItemChanged(holder.bindingAdapterPosition)
            callBack.onOrientationClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class SexualHolder(val binding: ItemEditSexualOrientationBinding) :
        RecyclerView.ViewHolder(binding.root)
}