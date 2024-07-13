package com.udemy.dependency.injection.app.repository

import com.udemy.dependency.injection.app.Constants
import com.udemy.dependency.injection.app.services.DatabaseService
import com.udemy.dependency.injection.app.services.NotificationService
import javax.inject.Inject
import javax.inject.Named

class UserRepository @Inject constructor(
    @Named(Constants.ROOM) private val databaseService: DatabaseService,
    @Named(Constants.MESSAGE) private val notificationService: NotificationService,
) {

    fun registerUser(email: String, password: String) {
        databaseService.saveUser(email, password)
        notificationService.send(email, "User Registration", "Successful Registration")
    }
}