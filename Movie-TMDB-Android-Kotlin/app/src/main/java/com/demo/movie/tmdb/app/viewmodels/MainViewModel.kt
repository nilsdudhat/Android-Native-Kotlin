package com.demo.movie.tmdb.app.viewmodels

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.movie.tmdb.app.activities.MovieDetailsActivity
import com.demo.movie.tmdb.app.models.Movie
import com.demo.movie.tmdb.app.models.PopularMovies
import com.demo.movie.tmdb.app.repositories.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MainFactory(val activity: Activity)

class MainViewModel(private val mainFactory: MainFactory) : ViewModel() {

    private val movieRepository = lazy { MovieRepository() }

    val data = MutableStateFlow<PopularMovies?>(null)

    fun getPopularMovies(pageNumber: Int) {
        viewModelScope.launch {
            val response = movieRepository.value.getPopularMovies(pageNumber)
            data.emit(response)
        }
    }

    fun onMovieClick(view: View, movie: Movie) {
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation(mainFactory.activity, view,"poster")
        val bundle = options.toBundle()

        val intent = Intent(mainFactory.activity, MovieDetailsActivity::class.java)
        intent.putExtra("movie", movie)
        mainFactory.activity.startActivity(intent, bundle)
    }
}