package com.demo.movie.tmdb.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.movie.tmdb.app.models.MovieDetails
import com.demo.movie.tmdb.app.repositories.MovieRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MovieDetailsViewModel : ViewModel() {

    private val movieRepository = lazy { MovieRepository() }

    fun getMovieDetails(movieID: Int): MutableSharedFlow<MovieDetails?> {
        val data = MutableStateFlow<MovieDetails?>(null)

        viewModelScope.launch {
            val response = movieRepository.value.getMovieDetails(movieID)
            data.emit(response)
        }

        return data
    }
}