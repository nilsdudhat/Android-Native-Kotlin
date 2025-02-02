package com.demo.movie.tmdb.app.repositories

import com.demo.movie.tmdb.app.api.APIInterface
import com.demo.movie.tmdb.app.models.MovieDetails
import com.demo.movie.tmdb.app.models.PopularMovies
import retrofit2.Response

class MovieRepository(private val apiInterface: APIInterface) {

    suspend fun getPopularMovies(pageNumber: Int): Response<PopularMovies?> {
        return apiInterface.getPopularMovies(pageNumber)
    }

    suspend fun getMovieDetails(movieID: Int): Response<MovieDetails?> {
        return apiInterface.getMovieDetails(movieID)
    }
}