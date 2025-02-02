package com.demo.movie.tmdb.app.utils

import android.graphics.Bitmap
import android.icu.text.DecimalFormat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.demo.movie.tmdb.app.models.Genre
import com.demo.movie.tmdb.app.models.SpokenLanguage

@BindingAdapter("poster_path")
fun loadPoster(view: ImageView, poster_path: String?) {
    if (poster_path != null) {
        view.setBackgroundResource(android.R.color.transparent)

        // Create circular progress programmatically
        val parentView = view.parent as ViewGroup

        // Remove any existing progress bar first
        parentView.children
            .filterIsInstance<ProgressBar>()
            .forEach { parentView.removeView(it) }

        var progressIndicator: ProgressBar?

        progressIndicator = ProgressBar(getKoinContext()).apply {
            id = View.generateViewId()
            layoutParams = when (parentView) {
                is ConstraintLayout -> ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                )

                is FrameLayout -> FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                )

                is LinearLayout -> LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    gravity = Gravity.CENTER
                }

                else -> ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
        }

        // Add progress indicator to parent
        parentView.addView(progressIndicator)

        when (parentView) {
            is ConstraintLayout -> centerInConstraintLayout(parentView, progressIndicator, view)
            is FrameLayout -> centerInFrameLayout(parentView, progressIndicator)
            is LinearLayout -> centerInLinearLayout(parentView, progressIndicator)
        }

        // Function to safely remove progress indicator
        fun removeProgress() {
            progressIndicator.let { progress ->
                progress?.visibility = View.GONE
                parentView.post {
                    try {
                        if (progress?.parent != null) {
                            parentView.removeView(progress)
                        }
                    } catch (e: Exception) {
                        // Handle any potential exceptions during removal
                    }
                }
                progressIndicator = null
            }
        }

        getGlideManager()
            .asBitmap()
            .load("${Constants.POSTER_BASE_URL}$poster_path")
            .addListener(object : RequestListener<Bitmap> {

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    removeProgress()
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>,
                    isFirstResource: Boolean,
                ): Boolean {
                    removeProgress()
                    return false
                }
            })
            .into(view)
    }
}

@BindingAdapter("backdrop_path")
fun loadBackDrop(view: ImageView, backdrop_path: String?) {
    if (backdrop_path != null) {
        view.setBackgroundResource(android.R.color.transparent)

        // Create circular progress programmatically
        val parentView = view.parent as ViewGroup

        // Remove any existing progress bar first
        parentView.children
            .filterIsInstance<ProgressBar>()
            .forEach { parentView.removeView(it) }

        var progressIndicator: ProgressBar?

        progressIndicator = ProgressBar(getKoinContext()).apply {
            id = View.generateViewId()
            layoutParams = when (parentView) {
                is ConstraintLayout -> ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                )

                is FrameLayout -> FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                )

                is LinearLayout -> LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    gravity = Gravity.CENTER
                }

                else -> ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
        }

        // Add progress indicator to parent
        parentView.addView(progressIndicator)

        when (parentView) {
            is ConstraintLayout -> centerInConstraintLayout(parentView, progressIndicator, view)
            is FrameLayout -> centerInFrameLayout(parentView, progressIndicator)
            is LinearLayout -> centerInLinearLayout(parentView, progressIndicator)
        }

        // Function to safely remove progress indicator
        fun removeProgress() {
            progressIndicator.let { progress ->
                progress?.visibility = View.GONE
                parentView.post {
                    try {
                        if (progress?.parent != null) {
                            parentView.removeView(progress)
                        }
                    } catch (e: Exception) {
                        // Handle any potential exceptions during removal
                    }
                }
                progressIndicator = null
            }
        }

        getGlideManager()
            .asBitmap()
            .load("${Constants.BACKDROP_BASE_URL}$backdrop_path")
            .addListener(object : RequestListener<Bitmap> {

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    removeProgress()
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>,
                    isFirstResource: Boolean,
                ): Boolean {
                    removeProgress()
                    return false
                }
            })
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