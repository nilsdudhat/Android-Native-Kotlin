package com.demo.movie.tmdb.app.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.demo.movie.tmdb.app.databinding.ActivityMovieDetailsBinding
import com.demo.movie.tmdb.app.models.Movie
import com.demo.movie.tmdb.app.utils.Status
import com.demo.movie.tmdb.app.utils.showSnackBar
import com.demo.movie.tmdb.app.viewmodels.MovieDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MovieDetailsActivity : AppCompatActivity() {

    val binding: ActivityMovieDetailsBinding by lazy {
        ActivityMovieDetailsBinding.inflate(layoutInflater)
    }

    private val movieDetailsViewModel : MovieDetailsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
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

        lifecycleScope.launch(Dispatchers.IO) {
            movieDetailsViewModel.getMovieDetails(movie!!.id).collect {
                lifecycleScope.launch(Dispatchers.Main) {
                    when(it.status) {
                        Status.SUCCESS -> {
                            binding.isSkeleton = false
                            binding.movieDetails = it.data
                        }
                        Status.ERROR -> {
                            binding.isSkeleton = false
                            it.message?.let { it1 -> showSnackBar(it1) }
                        }
                        Status.LOADING -> {
                            binding.isSkeleton = true
                        }
                    }
                }
            }
        }
    }
}