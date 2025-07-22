package com.belive.dating.activities.filter.location.search_location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemSearchLocationBinding
import com.belive.dating.extensions.Status
import com.belive.dating.extensions.getKoinActivity
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.hideKeyboard
import com.belive.dating.extensions.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchLocationAdapter(private val viewModel: SearchLocationViewModel) :
    RecyclerView.Adapter<SearchLocationAdapter.ViewHolder>() {

    init {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.placeData.collectLatest {
                launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.LOADING -> {

                        }

                        Status.SIGN_OUT -> {

                        }

                        Status.ADMIN_BLOCKED -> {

                        }

                        Status.ERROR -> {

                        }

                        Status.SUCCESS -> {
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = viewModel.placeData.value.data?.get(position)

        logger("--place--", gsonString(place))

        holder.binding.title = place?.displayName
        holder.binding.address = place?.addressComponents?.asList()?.joinToString(", ") { it.name }
        holder.binding.latitude = place?.location?.latitude.toString()
        holder.binding.longitude = place?.location?.longitude.toString()
        holder.binding.isSelected = viewModel.selectedPlace == place

        holder.binding.root.setOnClickListener {
            getKoinActivity().hideKeyboard()

            if (viewModel.selectedPlace.value == place) {
                viewModel.selectedPlace.postValue(null)
                holder.binding.isSelected = false
                return@setOnClickListener
            }
            val previousSelectedPlace = viewModel.selectedPlace.value
            viewModel.selectedPlace.postValue(place)
            viewModel.placeData.value.data?.indexOf(previousSelectedPlace)?.let { it1 -> notifyItemChanged(it1) }
            holder.binding.isSelected = true
        }
    }

    override fun getItemCount(): Int {
        return viewModel.placeData.value.data?.size ?: 0
    }

    class ViewHolder(val binding: ItemSearchLocationBinding) : RecyclerView.ViewHolder(binding.root)
}