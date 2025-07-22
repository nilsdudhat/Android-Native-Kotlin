package com.belive.dating.di

import com.belive.dating.api.gist.GISTRepository
import com.belive.dating.api.gist.GISTService
import com.belive.dating.api.gist.GistClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val gistModule = module {
    single(named("gist")) {
        GistClient.getGistInstance(get())
    }
    single {
        get<Retrofit>(named("gist")).create(GISTService::class.java)
    }
    single {
        GISTRepository()
    }
}