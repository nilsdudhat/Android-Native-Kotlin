package com.udemy.hilt.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.udemy.hilt.app.models.Product

@Database(entities = [Product::class], version = 1)
abstract class FakerDB : RoomDatabase() {

    abstract fun getFakerDAO() : FakerDAO

}