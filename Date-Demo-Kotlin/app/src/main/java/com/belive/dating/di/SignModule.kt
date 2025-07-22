package com.belive.dating.di

import com.belive.dating.helpers.helper_functions.authentication.AuthenticationHelper
import org.koin.dsl.module

val authenticationHelperModule = module {
    single {
        AuthenticationHelper()
    }
}