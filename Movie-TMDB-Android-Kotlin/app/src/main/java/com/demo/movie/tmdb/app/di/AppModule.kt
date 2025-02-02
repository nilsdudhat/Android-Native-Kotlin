package com.demo.movie.tmdb.app.di

import android.content.Context
import com.bumptech.glide.Glide
import com.demo.movie.tmdb.app.api.APIClient
import com.demo.movie.tmdb.app.api.APIInterface
import com.demo.movie.tmdb.app.helpers.CurrentActivityHolder
import com.demo.movie.tmdb.app.repositories.MovieRepository
import com.demo.movie.tmdb.app.viewmodels.MainViewModel
import com.demo.movie.tmdb.app.viewmodels.MovieDetailsViewModel
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val appModule = module {
    single {
        CurrentActivityHolder()
    }
    single {
        Retrofit.Builder()
    }
    single {
        Glide.with(get<Context>())
    }
    single {
        APIClient.getHttpClient()
    }
    single {
        APIClient.getInstance(get<OkHttpClient.Builder>(), get<Retrofit.Builder>())
    }
    single {
        get<Retrofit>().create(APIInterface::class.java)
    }
    single {
        MovieRepository(get<APIInterface>())
    }
    viewModel {
        MainViewModel()
    }
    viewModel {
        MovieDetailsViewModel()
    }
}