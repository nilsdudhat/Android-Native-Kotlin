package com.demo.movie.tmdb.app.repositories

import com.demo.movie.tmdb.app.api.APIClient
import com.demo.movie.tmdb.app.api.APIInterface
import com.demo.movie.tmdb.app.models.MovieDetails
import com.demo.movie.tmdb.app.models.PopularMovies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class MovieRepository {

    private val apiClient = lazy { APIClient.getInstance().create(APIInterface::class.java) }

    suspend fun getPopularMovies(pageNumber: Int): Response<PopularMovies?> {

        val response = withContext(Dispatchers.IO) {
            return@withContext apiClient.value.getPopularMovies(pageNumber)
        }

        return response
    }

    suspend fun getMovieDetails(movieID: Int): Response<MovieDetails?> {

        val response = withContext(Dispatchers.IO) {
            return@withContext apiClient.value.getMovieDetails(movieID)
        }

        return response
    }
}