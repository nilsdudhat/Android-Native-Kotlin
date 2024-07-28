package com.demo.movie.tmdb.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.movie.tmdb.app.models.MovieDetails
import com.demo.movie.tmdb.app.repositories.MovieRepository
import kotlinx.coroutines.launch

class MovieDetailsViewModel : ViewModel() {

    private val movieRepository = lazy { MovieRepository() }

    fun getMovieDetails(movieID: Int): LiveData<MovieDetails?> {

        val data = MutableLiveData<MovieDetails?>()

        viewModelScope.launch {
            val response = movieRepository.value.getMovieDetails(movieID)
            data.postValue(response)
        }

        return data
    }

}