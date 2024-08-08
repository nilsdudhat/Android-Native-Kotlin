package com.demo.api.app.university

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UniversityInterface {

    @GET("search")
    suspend fun getUniversities(@Query("country") countryName: String): Response<ArrayList<University>>
}