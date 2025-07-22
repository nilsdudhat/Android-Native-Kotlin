package com.belive.dating.activities.edit_profile.basics.education

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.EducationData
import com.belive.dating.databinding.ItemEditEducationBinding

/**
 * Adapter for displaying and managing a list of education items in an edit screen.
 *
 * This adapter uses `AsyncListDiffer` for efficient updates to the RecyclerView and allows
 * the user to select a single education item from the list.  It communicates the selected
 * item's position back to the `EditEducationViewModel` and notifies the provided
 * `OnEducationChangeListener` when an item is clicked.
 *
 * @property callBack An interface for handling education item clicks.
 * @property viewModel The ViewModel associated with the edit education screen, responsible
 *                    for managing the selected education item and data.
 */
class EditEducationAdapter(
    private var callBack: OnEducationChangeListener,
    private val viewModel: EditEducationViewModel,
) : RecyclerView.Adapter<EditEducationAdapter.ViewHolder>() {

    interface OnEducationChangeListener {
        fun onEducationClick(position: Int)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<EducationData>() {
        override fun areItemsTheSame(oldItem: EducationData, newItem: EducationData): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name)
        }

        override fun areContentsTheSame(oldItem: EducationData, newItem: EducationData): Boolean {
            return oldItem == newItem
        }
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEditEducationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.position = position
        holder.binding.name = asyncListDiffer.currentList[position].name
        holder.binding.isSelected = viewModel.selectedEducation.get() == asyncListDiffer.currentList[position].id
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            callBack.onEducationClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class ViewHolder(val binding: ItemEditEducationBinding) : RecyclerView.ViewHolder(binding.root)
}