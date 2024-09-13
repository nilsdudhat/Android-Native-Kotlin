package com.eventbus.app

import org.koin.dsl.module

val appModule = module {
    single { AppEventBus() }
}