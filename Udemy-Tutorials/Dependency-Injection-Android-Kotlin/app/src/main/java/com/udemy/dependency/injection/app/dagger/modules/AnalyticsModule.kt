package com.udemy.dependency.injection.app.dagger.modules

import com.udemy.dependency.injection.app.services.AnalyticsService
import com.udemy.dependency.injection.app.services.FirebaseAnalytics
import com.udemy.dependency.injection.app.services.Mixpanel
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class AnalyticsModule {

    @Provides
    @Singleton
    @Named("mixpanel")
    fun getMixpanel(): AnalyticsService {
        return Mixpanel()
    }

    @Provides
    @Singleton
    @Named("firebase")
    fun getFirebaseAnalytics(): AnalyticsService {
        return FirebaseAnalytics()
    }
}