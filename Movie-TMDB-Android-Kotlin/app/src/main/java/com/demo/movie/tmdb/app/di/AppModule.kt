package com.demo.movie.tmdb.app.di

import com.demo.movie.tmdb.app.api.APIClient
import com.demo.movie.tmdb.app.repositories.MovieRepository
import com.demo.movie.tmdb.app.viewmodels.MainViewModel
import com.demo.movie.tmdb.app.viewmodels.MovieDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        APIClient.getInstance()
    }
    single {
        APIClient.getHttpClient()
    }
    single {
        MovieRepository()
    }
    viewModel {
        MainViewModel(it.get())
    }
    viewModel {
        MovieDetailsViewModel()
    }
}