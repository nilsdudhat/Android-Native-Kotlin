package com.demo.movie.tmdb.app.utils

import android.icu.text.DecimalFormat
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.demo.movie.tmdb.app.models.Genre
import com.demo.movie.tmdb.app.models.SpokenLanguage

@BindingAdapter("poster_path")
fun loadPoster(view: ImageView, posterPath: String?) {
    loadImageWithProgressBar(view, "${Constants.POSTER_BASE_URL}$posterPath")
}

@BindingAdapter("backdrop_path")
fun loadBackDrop(view: ImageView, backdropPath: String?) {
    loadImageWithProgressBar(view, "${Constants.BACKDROP_BASE_URL}$backdropPath")
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