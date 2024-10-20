package com.demo.movie.tmdb.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.movie.tmdb.app.databinding.ItemListBinding
import com.demo.movie.tmdb.app.interfaces.OnLastViewAttached
import com.demo.movie.tmdb.app.models.Movie
import com.demo.movie.tmdb.app.viewmodels.MainViewModel

class ListAdapter(
    private val onLastViewAttached: OnLastViewAttached,
    private val viewModel: MainViewModel
) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    var movieList: java.util.ArrayList<Movie> = java.util.ArrayList()
        set(value) {
            val size = field.size
            field = value
            notifyItemRangeChanged(size, value.size - 1)
        }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        if (holder.adapterPosition == (movieList.size - 1)) {
            onLastViewAttached.onLastViewAttach()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemListBinding =
            ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movieList[position]
        holder.binding.movie = movie
        holder.binding.viewModel = viewModel
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    class ViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)
}