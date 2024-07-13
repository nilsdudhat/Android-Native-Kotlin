package com.udemy.dependency.injection.app.dagger.modules

import com.udemy.dependency.injection.app.Constants
import com.udemy.dependency.injection.app.dagger.annotations.ActivityScope
import com.udemy.dependency.injection.app.services.DatabaseService
import com.udemy.dependency.injection.app.services.FirebaseDatabaseService
import com.udemy.dependency.injection.app.services.RoomDatabaseService
import dagger.Binds
import dagger.Module
import javax.inject.Named

@Module
abstract class DatabaseModule {

    /*
    * Binds is used to return the instance of the class,
    * cannot be used while parsing arguments,
    * use Provides in such case
    * */
    @Binds
    @ActivityScope
    @Named(Constants.FIREBASE)
    abstract fun getFirebaseDatabaseService(firebaseDatabaseService: FirebaseDatabaseService): DatabaseService

    @Binds
    @ActivityScope
    @Named(Constants.ROOM)
    abstract fun getRoomDatabaseService(roomDatabaseService: RoomDatabaseService): DatabaseService

    /*
    * Provides is used to return the instance of the class,
    * must use while need of parsing arguments
    * */
    /*
    @Provides
    fun getRoomDatabaseService(): DatabaseService {
        return RoomDatabaseService()
    }
    */
}