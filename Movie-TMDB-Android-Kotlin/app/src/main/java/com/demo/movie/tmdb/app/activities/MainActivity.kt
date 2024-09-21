package com.demo.movie.tmdb.app.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.demo.movie.tmdb.app.R
import com.demo.movie.tmdb.app.databinding.ActivityMainBinding
import com.demo.movie.tmdb.app.models.Movie
import com.demo.movie.tmdb.app.utils.KeepStateNavigator
import com.demo.movie.tmdb.app.utils.ProgressUtils
import com.demo.movie.tmdb.app.utils.createFactory
import com.demo.movie.tmdb.app.utils.observeOnce
import com.demo.movie.tmdb.app.viewmodels.MainFactory
import com.demo.movie.tmdb.app.viewmodels.MainViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val mainViewModel = lazy {
        ViewModelProvider(
            this,
            MainViewModel(MainFactory(this)).createFactory()
        )[MainViewModel::class.java]
    }

    val movieListData = MutableLiveData<ArrayList<Movie>?>()

    var totalPages = 0
    var currentPage = 0

    private lateinit var appBarConfiguration: AppBarConfiguration

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

        ProgressUtils.showLoading(this)
        getPopularMovies(1)
    }

    fun getPopularMovies(pageNumber: Int) {
        Log.d("--timer--", "getPopularMovies: " + System.currentTimeMillis())
        mainViewModel.value.getPopularMovies(pageNumber).observeOnce(this) {
            ProgressUtils.hideLoading()

            Log.d("--timer--", "getPopularMovies: " + System.currentTimeMillis())
            Log.d("--timer--", "getPopularMovies: $it")

            val movies = it?.results
            if (movies != null) {
                var oldMovies = movieListData.value
                if (oldMovies == null) {
                    oldMovies = ArrayList()
                }
                oldMovies.addAll(movies)
                movieListData.value = oldMovies
            }

            currentPage = pageNumber
            totalPages = it?.total_pages ?: 0
        }
    }

    private fun setUpToolbar() {

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.list_fragment, R.id.grid_fragment))

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener it.onNavDestinationSelected(navController) ||
                    super.onOptionsItemSelected(it)
        }
    }
}