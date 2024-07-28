package com.demo.movie.tmdb.app.repositories

import com.demo.movie.tmdb.app.api.APIClient
import com.demo.movie.tmdb.app.api.APIInterface
import com.demo.movie.tmdb.app.models.MovieDetails
import com.demo.movie.tmdb.app.models.PopularMovies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepository {

    private val apiClient = lazy { APIClient.getInstance().create(APIInterface::class.java) }

    suspend fun getPopularMovies(pageNumber: Int): PopularMovies? {

        val response = withContext(Dispatchers.IO) {
            return@withContext apiClient.value.getPopularMovies(pageNumber)
        }

        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun getMovieDetails(movieID: Int): MovieDetails? {

        val response = withContext(Dispatchers.IO) {
            return@withContext apiClient.value.getMovieDetails(movieID)
        }

        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}