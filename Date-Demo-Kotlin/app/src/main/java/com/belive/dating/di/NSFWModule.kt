package com.belive.dating.di

import com.belive.dating.api.nsfw.NSFWClient
import com.belive.dating.api.nsfw.NSFWRepository
import com.belive.dating.api.nsfw.NSFWService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val nsfwModule = module {
    single(named("nsfw")) {
        NSFWClient.getNSFWInstance()
    }
    single {
        get<Retrofit>(named("nsfw")).create(NSFWService::class.java)
    }
    single {
        NSFWRepository()
    }
}