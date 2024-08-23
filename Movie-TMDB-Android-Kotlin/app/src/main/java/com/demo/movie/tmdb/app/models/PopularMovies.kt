package com.demo.movie.tmdb.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PopularMovies(
    val page: Int,
    val results: List<Movie>,
    val total_pages: Int,
    val total_results: Int,
) : Parcelable