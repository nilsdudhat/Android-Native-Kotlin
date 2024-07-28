package com.demo.movie.tmdb.app.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.demo.movie.tmdb.app.R
import com.demo.movie.tmdb.app.databinding.ActivityMovieDetailsBinding
import com.demo.movie.tmdb.app.models.Movie
import com.demo.movie.tmdb.app.utils.ProgressUtils
import com.demo.movie.tmdb.app.utils.observeOnce
import com.demo.movie.tmdb.app.viewmodels.MovieDetailsViewModel

class MovieDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailsBinding

    private val movieDetailsViewModel =
        lazy { ViewModelProvider(this)[MovieDetailsViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val movie: Movie? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", Movie::class.java)
        } else {
            intent.getParcelableExtra("movie")
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        ProgressUtils.showLoading(this)
        movieDetailsViewModel.value.getMovieDetails(movie!!.id).observeOnce(this) {
            ProgressUtils.hideLoading()
            binding.movieDetails = it
        }
    }
}