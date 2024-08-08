package com.demo.api.app.main

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.demo.api.app.age.AgeActivity
import com.demo.api.app.cat.CatActivity
import com.demo.api.app.coindesk.CoinActivity
import com.demo.api.app.country.CountryActivity
import com.demo.api.app.dogimage.DogImageActivity
import com.demo.api.app.gender.GenderActivity
import com.demo.api.app.joke.JokeActivity
import com.demo.api.app.ransomuser.RandomUserActivity
import com.demo.api.app.university.UniversitiesActivity
import com.demo.api.app.zipcode.ZipCodeActivity

data class MainFactory(val activity: Activity)

class MainViewModel(private val mainFactory: MainFactory) : ViewModel() {

    private val list = mutableListOf<Map<String, String>>()

    init {
        list.add(
            mapOf(
                "name" to "Universities List",
                "sample_url" to "http://universities.hipolabs.com/search?country=United+States"
            )
        )
        list.add(
            mapOf(
                "name" to "Area Identifier",
                "sample_url" to "https://api.zippopotam.us/us/33162"
            )
        )
        list.add(mapOf("name" to "Random User", "sample_url" to "https://randomuser.me/api/"))
        list.add(
            mapOf(
                "name" to "Dog Image",
                "sample_url" to "https://dog.ceo/api/breeds/image/random"
            )
        )
        list.add(
            mapOf(
                "name" to "Joke",
                "sample_url" to "https://official-joke-api.appspot.com/random_joke"
            )
        )
        list.add(
            mapOf(
                "name" to "Gender Identifier",
                "sample_url" to "https://api.genderize.io/?name=luc"
            )
        )
        list.add(
            mapOf(
                "name" to "Country Identifier",
                "sample_url" to "https://api.nationalize.io/?name=nil"
            )
        )
        list.add(mapOf("name" to "Cat Facts", "sample_url" to "https://catfact.ninja/fact"))
        list.add(mapOf("name" to "Coin Desk", "sample_url" to "http://www.coindesk.com/api/"))
        list.add(
            mapOf(
                "name" to "Age Identifier",
                "sample_url" to "https://api.agify.io/?name=Tripti"
            )
        )
    }

    fun getAPIList(): MutableList<Map<String, String>> {
        return list
    }

    fun onClick(main: Map<String, String>) {
        val intent: Intent
        if (main["sample_url"].toString().startsWith("http://universities.hipolabs.com/")) {
            intent = Intent(mainFactory.activity, UniversitiesActivity::class.java)
        } else if (main["sample_url"].toString().startsWith("https://api.zippopotam.us/")) {
            intent = Intent(mainFactory.activity, ZipCodeActivity::class.java)
        } else if (main["sample_url"].toString().startsWith("https://randomuser.me/api/")) {
            intent = Intent(mainFactory.activity, RandomUserActivity::class.java)
        } else if (main["sample_url"].toString().startsWith("https://dog.ceo/api/")) {
            intent = Intent(mainFactory.activity, DogImageActivity::class.java)
        } else if (main["sample_url"].toString().startsWith("https://official-joke-api.appspot.com/")) {
            intent = Intent(mainFactory.activity, JokeActivity::class.java)
        } else if (main["sample_url"].toString().startsWith("https://api.genderize.io/")) {
            intent = Intent(mainFactory.activity, GenderActivity::class.java)
        } else if (main["sample_url"].toString().startsWith("https://api.nationalize.io/")) {
            intent = Intent(mainFactory.activity, CountryActivity::class.java)
        } else if (main["sample_url"].toString().startsWith("https://catfact.ninja/")) {
            intent = Intent(mainFactory.activity, CatActivity::class.java)
        } else if (main["sample_url"].toString().startsWith("http://www.coindesk.com/api/")) {
            intent = Intent(mainFactory.activity, CoinActivity::class.java)
        } else if (main["sample_url"].toString().startsWith("https://api.agify.io/")) {
            intent = Intent(mainFactory.activity, AgeActivity::class.java)
        } else {
            intent = Intent()
        }
        mainFactory.activity.startActivity(intent)
    }
}