package com.udemy.dependency.injection.app.dagger.component

import com.udemy.dependency.injection.app.activities.MainActivity
import com.udemy.dependency.injection.app.dagger.annotations.ActivityScope
import com.udemy.dependency.injection.app.dagger.modules.DatabaseModule
import com.udemy.dependency.injection.app.dagger.modules.NotificationServiceModule
import dagger.BindsInstance
import dagger.Component

/*
* Need to all the modules to use in an component
* */
@ActivityScope
@Component(
    dependencies = [AppComponent::class],
    modules = [NotificationServiceModule::class, DatabaseModule::class]
)
interface UserComponent {

    /*
    * applies for whole activity
    * */
    fun inject(activity: MainActivity)

    /*
    * Factory object prevents runtime errors for dagger component initialisation,
    * and helps to easy pass of arguments in modules
    * */
    @Component.Factory
    interface Factory {
        fun createFactory(@BindsInstance retryCount: Int, appComponent: AppComponent): UserComponent
    }
}