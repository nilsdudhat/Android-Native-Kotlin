package com.belive.dating.di

import com.belive.dating.api.introduction.IntroductionClient
import com.belive.dating.api.introduction.IntroductionRepository
import com.belive.dating.api.introduction.IntroductionService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val introductionModule = module {
    single(named("introduction")) {
        IntroductionClient.getIntroductionInstance()
    }
    single {
        get<Retrofit>(named("introduction")).create(IntroductionService::class.java)
    }
    single {
        IntroductionRepository()
    }
}