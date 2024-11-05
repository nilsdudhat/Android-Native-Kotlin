package com.demo.movie.tmdb.app.utils

import android.icu.text.DecimalFormat
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.demo.movie.tmdb.app.models.Genre
import com.demo.movie.tmdb.app.models.SpokenLanguage

@BindingAdapter("poster_path")
fun loadPoster(view: ImageView, poster_path: String?) {
    if (poster_path != null) {
        Glide.with(view)
            .load("${Constants.POSTER_BASE_URL}$poster_path")
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(view)
    }
}

@BindingAdapter("backdrop_path")
fun loadBackDrop(view: ImageView, backdrop_path: String?) {
    if (backdrop_path != null) {
        Glide.with(view)
            .load("${Constants.BACKDROP_BASE_URL}$backdrop_path")
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(view)
    }
}

@BindingAdapter("vote_average")
fun voteAverage(view: TextView, vote_average: Double?) {
    if (vote_average != null) {
        view.text = DecimalFormat("#.##").format(vote_average)
    }
}

@BindingAdapter("genres")
fun genres(view: TextView, genres: List<Genre>?) {
    var genresString = ""
    if (genres != null) {
        for (genre in genres) {
            genresString = if (genresString.isEmpty()) {
                genre.name
            } else {
                "$genresString, ${genre.name}"
            }
        }
    }
    view.text = genresString
}

@BindingAdapter("spoken_languages")
fun languages(view: TextView, languages: List<SpokenLanguage>?) {
    var languagesString = ""

    if (languages != null) {
        for (language in languages) {
            languagesString = if (languagesString.isEmpty()) {
                language.name
            } else {
                "$languagesString, ${language.name}"
            }
        }
    }

    view.text = languagesString
}