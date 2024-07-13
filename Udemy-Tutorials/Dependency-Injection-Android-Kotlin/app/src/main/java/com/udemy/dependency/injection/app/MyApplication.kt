package com.udemy.dependency.injection.app

import android.app.Application
import com.udemy.dependency.injection.app.dagger.component.AppComponent
import com.udemy.dependency.injection.app.dagger.component.DaggerAppComponent

class MyApplication: Application() {

//    lateinit var userComponent: UserComponent
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

//        userComponent = DaggerUserComponent.factory().createFactory(5)
        appComponent = DaggerAppComponent.builder().build()
    }
}