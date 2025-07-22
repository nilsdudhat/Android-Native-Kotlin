package com.belive.dating.activities.edit_profile.basics.zodiac

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.ZodiacData
import com.belive.dating.databinding.ItemEditZodiacBinding
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * Adapter for displaying and handling zodiac selections in a RecyclerView.
 *
 * This adapter uses DiffUtil for efficient updates and FlexboxLayoutManager
 * for flexible layout of zodiac items.  It allows the user to select a zodiac,
 * highlighting the selected item and notifying a callback of the selection change.
 *
 * @property callBack Interface for handling zodiac selection changes.
 * @property viewModel ViewModel holding the current selected zodiac.
 */
class EditZodiacAdapter(
    private var callBack: OnZodiacChangeListener,
    private val viewModel: EditZodiacViewModel,
) : RecyclerView.Adapter<EditZodiacAdapter.ViewHolder>() {

    interface OnZodiacChangeListener {
        fun onZodiacClick(position: Int)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<ZodiacData>() {
        override fun areItemsTheSame(oldItem: ZodiacData, newItem: ZodiacData): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name) && (oldItem.icon == newItem.icon)
        }

        override fun areContentsTheSame(oldItem: ZodiacData, newItem: ZodiacData): Boolean {
            return oldItem == newItem
        }
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditZodiacBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lp: ViewGroup.LayoutParams = holder.binding.main.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.flexGrow = 1.0f
            lp.alignSelf = AlignSelf.AUTO
        }

        holder.binding.position = position
        holder.binding.name = asyncListDiffer.currentList[position].name
        holder.binding.icon = asyncListDiffer.currentList[position].icon
        holder.binding.isSelected = viewModel.selectedZodiac.get() == asyncListDiffer.currentList[position].id
        holder.binding.executePendingBindings()

        holder.binding.main.setOnClickListener {
            callBack.onZodiacClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class ViewHolder(val binding: ItemEditZodiacBinding) : RecyclerView.ViewHolder(binding.root)
}