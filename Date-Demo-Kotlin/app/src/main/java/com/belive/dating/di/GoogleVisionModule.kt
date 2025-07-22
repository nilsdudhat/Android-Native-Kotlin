package com.belive.dating.di

import com.belive.dating.api.google_vision.GoogleVisionClient
import com.belive.dating.api.google_vision.GoogleVisionRepository
import com.belive.dating.api.google_vision.GoogleVisionService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val googleVisionModule = module {
    single(named("google-vision")) {
        GoogleVisionClient.getGoogleVisionInstance()
    }
    single {
        get<Retrofit>(named("google-vision")).create(GoogleVisionService::class.java)
    }
    single {
        GoogleVisionRepository()
    }
}