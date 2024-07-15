package com.udemy.hilt.app.retrofit

import com.udemy.hilt.app.models.Product
import retrofit2.Response
import retrofit2.http.GET

interface FakerAPI {

    @GET("products")
    suspend fun getProducts() : Response<List<Product>>
}