package com.demo.movie.tmdb.app.activities

import android.os.Bundle
import android.util.Log
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
        val navController = Navigation.findNavController(this, R.id.fragment_container)
        val navHostFragment: Fragment? = supportFragmentManager
            .findFragmentById(R.id.fragment_container)
        val childFragmentManager: FragmentManager = navHostFragment!!.getChildFragmentManager()

        binding.toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener {
            navController.navigatorProvider.addNavigator(
                KeepStateNavigator(
                    this,
                    childFragmentManager,
                    R.id.fragment_container
                )
            )

            navController.setGraph(R.navigation.navigation_main)

            Log.d("--fragments--", "current: " + navController.currentDestination?.id)
            Log.d("--fragments--", "list_fragment: " + R.id.list_fragment)
            Log.d("--fragments--", "grid_fragment: " + R.id.grid_fragment)

            when (it.itemId) {
                R.id.item_list -> {
                    if (navController.currentDestination?.id != R.id.list_fragment) {
                        navController.apply {
                            navigate(
                                R.id.action_grid_to_list,
                                null,
                                NavOptions.Builder()
                                    .setRestoreState(true)
                                    .setPopUpTo(
                                        graph.startDestinationId,
                                        saveState = true,
                                        inclusive = true,
                                    ).build()
                            )
                        }
                    }
                }

                R.id.item_grid -> {
                    if (navController.currentDestination?.id != R.id.grid_fragment) {
                        navController.apply {
                            navigate(
                                R.id.action_list_to_grid,
                                null,
                                NavOptions.Builder()
                                    .setRestoreState(true)
                                    .setPopUpTo(
                                        graph.startDestinationId,
                                        saveState = true,
                                        inclusive = true,
                                    ).build()
                            )
                        }
                    }
                }
            }
            return@OnMenuItemClickListener true
        })
    }
}