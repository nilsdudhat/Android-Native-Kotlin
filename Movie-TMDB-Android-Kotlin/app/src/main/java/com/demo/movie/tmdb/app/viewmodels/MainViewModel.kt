package com.demo.movie.tmdb.app.viewmodels

import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.movie.tmdb.app.R
import com.demo.movie.tmdb.app.activities.MovieDetailsActivity
import com.demo.movie.tmdb.app.helpers.CurrentActivityHolder
import com.demo.movie.tmdb.app.models.Movie
import com.demo.movie.tmdb.app.models.PopularMovies
import com.demo.movie.tmdb.app.repositories.MovieRepository
import com.demo.movie.tmdb.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin

class MainViewModel : ViewModel() {

    private val movieRepository = lazy { getKoin().get<MovieRepository>() }

    val data = MutableStateFlow<Resource<PopularMovies>>(Resource.loading(null))

    fun getPopularMovies(pageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            data.emit(Resource.loading(null))

            val response = movieRepository.value.getPopularMovies(pageNumber)

            if (response.isSuccessful) {
                data.emit(Resource.success(response.body()))
            } else {
                if (response.errorBody() != null) {
                    data.emit(Resource.error(response.errorBody()!!.string(), null))
                } else {
                    data.emit(Resource.error("Something went wrong", null))
                }
            }
        }
    }

    fun onMovieClick(view: View, movie: Movie) {
        val activity = getKoin().get<CurrentActivityHolder>().getCurrentActivity()

        val pairTitle = Pair.create<View, String>(view.findViewById(R.id.txt_title), "title")
        val pairPoster = Pair.create<View, String>(view.findViewById(R.id.img_poster), "poster")
        val options =
            activity
                ?.let {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        it,
                        pairTitle,
                        pairPoster
                    )
                }
        val bundle = options?.toBundle()

        val intent = Intent(activity, MovieDetailsActivity::class.java)
        intent.putExtra("movie", movie)
        activity?.startActivity(intent, bundle)
    }
}