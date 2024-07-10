package com.udemy.retrofit.app.api

import com.udemy.retrofit.app.models.Album
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JsonPlaceHolderInterface {

    @GET("albums")
    suspend fun getAlbums(@Query("userId") id: Int): Response<List<Album>>
}