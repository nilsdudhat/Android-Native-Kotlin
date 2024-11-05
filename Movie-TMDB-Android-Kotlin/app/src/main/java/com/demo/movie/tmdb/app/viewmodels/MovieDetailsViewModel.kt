package com.demo.movie.tmdb.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.movie.tmdb.app.models.MovieDetails
import com.demo.movie.tmdb.app.repositories.MovieRepository
import com.demo.movie.tmdb.app.utils.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MovieDetailsViewModel : ViewModel() {

    private val movieRepository = lazy { MovieRepository() }

    fun getMovieDetails(movieID: Int): MutableSharedFlow<Resource<MovieDetails?>> {
        val data = MutableStateFlow<Resource<MovieDetails?>>(Resource.loading(null))

        viewModelScope.launch {
            val response = movieRepository.value.getMovieDetails(movieID)

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

        return data
    }
}