package com.demo.movie.tmdb.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
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

    private var movieList = ArrayList<Movie>()

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

    fun setMovies(movieList: List<Movie>) {
        this.movieList = java.util.ArrayList(movieList)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)
}