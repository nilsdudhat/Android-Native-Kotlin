package com.belive.dating.activities.filter.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.Observable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.user.models.my_locations.MyLocation
import com.belive.dating.databinding.ItemLocationBinding
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger

class LocationAdapter(val viewModel: ChangeLocationViewModel, val listener: OnLocationClickListener) :
    RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    interface OnLocationClickListener {
        fun onLocationClick(location: MyLocation)
    }

    init {
        viewModel.isDeleteView.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                toggleDeleteView()
            }
        })
    }

    private var deleteList = mutableListOf<Int>()

    private val diffUtil = object : DiffUtil.ItemCallback<MyLocation>() {
        override fun areItemsTheSame(oldItem: MyLocation, newItem: MyLocation): Boolean {
            return (oldItem.id == newItem.id) && (oldItem.name == newItem.name) && (oldItem.latitude == newItem.latitude) && (oldItem.longitude == newItem.longitude)
        }

        override fun areContentsTheSame(oldItem: MyLocation, newItem: MyLocation): Boolean {
            return oldItem == newItem
        }
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = asyncListDiffer.currentList[position]
        logger("--my_location--", "location: $${gsonString(location)}")

        holder.binding.myLocation = location
        holder.binding.isDeleteView = viewModel.isDeleteView
        holder.binding.isSelected =
            if (viewModel.isCurrentLocationSelected.get() == true) false else (location.latitude.toDouble() == viewModel.selectedLocation.get()?.first) && (location.longitude.toDouble() == viewModel.selectedLocation.get()?.second) && (location.name == viewModel.selectedLocation.get()?.third)
        // (21.2326934, 72.8357136, DRK COOLING SYSTEM)
        holder.binding.layoutLocation.setOnClickListener {
            if (viewModel.isDeleteView.get() == true) {
                if (deleteList.contains(location.id)) {
                    deleteList.remove(location.id)
                } else {
                    deleteList.add(location.id)
                }
                holder.binding.deleteList = deleteList

                logger("--delete--", gsonString(deleteList))
            }
            listener.onLocationClick(location)
        }

        holder.binding.checkDelete.setOnCheckedChangeListener { _, isChecked ->
            holder.binding.layoutLocation.performClick()
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    private fun toggleDeleteView() {
        deleteList.clear()
        notifyItemRangeChanged(0, viewModel.customLocationList.get()!!.size)
    }

    fun getDeleteList(): List<Int> = deleteList

    class ViewHolder(val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root)
}