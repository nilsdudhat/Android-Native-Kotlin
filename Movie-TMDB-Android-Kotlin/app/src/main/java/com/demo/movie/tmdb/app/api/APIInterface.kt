package com.demo.movie.tmdb.app.api

import com.demo.movie.tmdb.app.models.MovieDetails
import com.demo.movie.tmdb.app.models.PopularMovies
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIInterface {

    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") pageNumber: Int) : Response<PopularMovies?>

    @GET("movie/{id}")
    suspend fun getMovieDetails(@Path("id") id: Int) : Response<MovieDetails?>
}