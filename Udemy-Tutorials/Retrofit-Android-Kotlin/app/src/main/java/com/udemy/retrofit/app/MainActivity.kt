package com.udemy.retrofit.app

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.liveData
import com.udemy.retrofit.app.api.JsonPlaceHolderInstance
import com.udemy.retrofit.app.api.JsonPlaceHolderInterface
import com.udemy.retrofit.app.models.Album
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val jsonPlaceHolderInterface = JsonPlaceHolderInstance
            .getInstance().create(JsonPlaceHolderInterface::class.java)

        val responseLiveData = liveData {
            val response = jsonPlaceHolderInterface.getAlbums(id = 1)
            emit(response)
        }

        responseLiveData.observe(this) {

            val albumList: List<Album> = it.body()!!

            for (album in albumList) {
                Log.d("--title--", "onCreate: ${album.title}")
            }
        }
    }
}