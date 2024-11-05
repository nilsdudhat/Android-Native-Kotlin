package com.demo.movie.tmdb.app.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.demo.movie.tmdb.app.R
import com.demo.movie.tmdb.app.databinding.ActivityMainBinding
import com.demo.movie.tmdb.app.models.Movie
import com.demo.movie.tmdb.app.utils.ProgressUtils
import com.demo.movie.tmdb.app.viewmodels.MainFactory
import com.demo.movie.tmdb.app.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val mainViewModel: MainViewModel
            by viewModel { parametersOf(MainFactory(this@MainActivity)) }

    val movieListData = MutableLiveData<ArrayList<Movie>?>(null)

    var totalPages = 0
    var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpToolbar()

        observer()
    }

    private fun observer() {
        ProgressUtils.showLoading(this)

        getPopularMovies(1)

        lifecycleScope.launch {
            mainViewModel.data.collect {
                lifecycleScope.launch(Dispatchers.Main) {
                    ProgressUtils.hideLoading()
                }
                Log.d("--timer--", "getPopularMovies: " + System.currentTimeMillis())
                Log.d("--timer--", "getPopularMovies: $it")

                val movies = it?.results
                if (movies != null) {
                    var oldMovies = movieListData.value
                    if (oldMovies == null) {
                        oldMovies = ArrayList()
                    }
                    oldMovies.addAll(movies)
                    movieListData.postValue(oldMovies)
                }

                currentPage = it?.page ?: 0
                totalPages = it?.total_pages ?: 0
            }
        }
    }

    fun getPopularMovies(pageNumber: Int) {
        Log.d("--timer--", "getPopularMovies: " + System.currentTimeMillis())
        mainViewModel.getPopularMovies(pageNumber)
    }

    private fun setUpToolbar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.list_fragment, R.id.grid_fragment))

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener it.onNavDestinationSelected(navController) ||
                    super.onOptionsItemSelected(it)
        }
    }
}