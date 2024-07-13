package com.udemy.hilt.app

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    fun saveUser(email: String, password: String)
}

@Singleton
class SQLRepository @Inject constructor() : UserRepository {
    override fun saveUser(email: String, password: String) {
        Log.d("--tag--", "User saved in SQLite")
    }
}

@Singleton
class FirebaseRepository @Inject constructor() : UserRepository {
    override fun saveUser(email: String, password: String) {
        Log.d("--tag--", "User saved in Firebase")
    }
}