package com.belive.dating.di

import com.belive.dating.api.user.UserClient
import com.belive.dating.api.user.UserRepository
import com.belive.dating.api.user.UserService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val userModule = module {
    single(named("user")) {
        UserClient.getUserInstance()
    }
    single {
        get<Retrofit>(named("user")).create(UserService::class.java)
    }
    single {
        UserRepository()
    }
}