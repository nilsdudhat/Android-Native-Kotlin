package com.udemy.dependency.injection.app.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.udemy.dependency.injection.app.MyApplication
import com.udemy.dependency.injection.app.R
import com.udemy.dependency.injection.app.dagger.component.DaggerUserComponent
import com.udemy.dependency.injection.app.dagger.modules.NotificationServiceModule
import com.udemy.dependency.injection.app.repository.UserRepository
import com.udemy.dependency.injection.app.services.EmailService
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var emailService: EmailService

    @Inject
    lateinit var emailService1: EmailService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val daggerUserComponent = DaggerUserComponent
//            .builder()
//            .notificationServiceModule(NotificationServiceModule())
//            .build()
            .factory().createFactory(10)

//        val daggerUserComponent = (application as MyApplication).userComponent
        daggerUserComponent.inject(this)

        userRepository.registerUser("email", "password")

        Log.d("--email--", "email: $emailService")
        Log.d("--email--", "email1: $emailService1")
    }
}