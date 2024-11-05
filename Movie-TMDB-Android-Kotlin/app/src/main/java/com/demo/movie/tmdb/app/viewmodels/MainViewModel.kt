package com.demo.movie.tmdb.app.viewmodels

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.movie.tmdb.app.activities.MovieDetailsActivity
import com.demo.movie.tmdb.app.models.Movie
import com.demo.movie.tmdb.app.models.PopularMovies
import com.demo.movie.tmdb.app.repositories.MovieRepository
import com.demo.movie.tmdb.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class MainFactory(val activity: Activity)

class MainViewModel(private val mainFactory: MainFactory) : ViewModel() {

    private val movieRepository = lazy { MovieRepository() }

    val data = MutableStateFlow<Resource<PopularMovies>>(Resource.loading(null))

    fun getPopularMovies(pageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            data.emit(Resource.loading(null))

            val response = movieRepository.value.getPopularMovies(pageNumber)

            if (response.isSuccessful) {
                data.emit(Resource.success(response.body()))
            } else {
                if (response.errorBody() != null) {
                    data.emit(Resource.error(response.errorBody()!!.string(), null))
                } else {
                    data.emit(Resource.error("Something went wrong", null))
                }
            }
        }
    }

    fun onMovieClick(view: View, movie: Movie) {
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation(mainFactory.activity, view, "poster")
        val bundle = options.toBundle()

        val intent = Intent(mainFactory.activity, MovieDetailsActivity::class.java)
        intent.putExtra("movie", movie)
        mainFactory.activity.startActivity(intent, bundle)
    }
}