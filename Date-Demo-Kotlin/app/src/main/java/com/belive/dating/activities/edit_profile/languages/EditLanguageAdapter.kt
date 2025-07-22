package com.belive.dating.activities.edit_profile.languages

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.introduction.models.LanguageData
import com.belive.dating.databinding.ItemEditLanguageBinding
import com.belive.dating.extensions.getKoinContext

/**
 * Adapter for the RecyclerView displaying a list of languages that can be edited.
 * Allows the user to select up to 3 languages. Uses DiffUtil for efficient updates.
 *
 * The adapter utilizes `AsyncListDiffer` to manage the list of `LanguageData` items and efficiently update the RecyclerView when changes occur.
 * It also implements a click listener to handle language selections, allowing users to toggle the selection state of each language.
 * The selection is limited to a maximum of 3 languages, and a toast message is displayed if the user attempts to exceed this limit.
 *
 * @param callBack Listener to handle language selection events. This listener is triggered when a language is selected or deselected.
 *                The [OnLanguageListener.onLanguageClick] method receives an ArrayList of strings representing the currently selected languages.
 */
class EditLanguageAdapter(private val callBack: OnLanguageListener) :
    RecyclerView.Adapter<EditLanguageAdapter.ViewHolder>() {

    interface OnLanguageListener {
        fun onLanguageClick(list: ArrayList<String>)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<LanguageData>() {
        override fun areItemsTheSame(oldItem: LanguageData, newItem: LanguageData): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name) && (oldItem.isChecked == newItem.isChecked)
        }

        override fun areContentsTheSame(oldItem: LanguageData, newItem: LanguageData): Boolean {
            return oldItem == newItem
        }
    }

    var asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEditLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.position = position
        holder.binding.name = asyncListDiffer.currentList[position].name
        holder.binding.isSelected =
            if (asyncListDiffer.currentList[position].isChecked == null) false else asyncListDiffer.currentList[position].isChecked!!
        holder.binding.executePendingBindings()

        holder.binding.layoutMain.setOnClickListener {
            asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked =
                if (asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked == null) true else !asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked!!

            if (asyncListDiffer.currentList.count { it.isChecked == true } > 3) {
                asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked =
                    !asyncListDiffer.currentList[holder.bindingAdapterPosition].isChecked!!

                Toast.makeText(getKoinContext(), "You can select maximum of 3 languages", Toast.LENGTH_SHORT).show()
            } else {
                holder.binding.isSelected =
                    if (asyncListDiffer.currentList[position].isChecked == null) false else asyncListDiffer.currentList[position].isChecked!!
                callBack.onLanguageClick(getSelected())
            }
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    fun getSelected(): ArrayList<String> {
        val selected = arrayListOf<String>()
        for (i in asyncListDiffer.currentList.indices) {
            if (asyncListDiffer.currentList[i].isChecked == true) {
                selected.add(asyncListDiffer.currentList[i].name)
            }
        }
        return selected
    }

    class ViewHolder(val binding: ItemEditLanguageBinding) : RecyclerView.ViewHolder(binding.root)
}