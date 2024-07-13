package com.udemy.dependency.injection.app.dagger.component

import com.udemy.dependency.injection.app.dagger.modules.AnalyticsModule
import com.udemy.dependency.injection.app.services.AnalyticsService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AnalyticsModule::class])
interface AppComponent {

    fun getAnalyticsService(): AnalyticsService
}