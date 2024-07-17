package com.udemy.journal.app.viewmodels

import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udemy.journal.app.activities.LoginActivity
import com.udemy.journal.app.databinding.ActivitySignUpBinding
import com.udemy.journal.app.repositories.FirebaseRepository
import com.udemy.journal.app.utils.hideKeyboard
import com.udemy.journal.app.utils.isEmailValid
import com.udemy.journal.app.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class SignUpFactory(
    val activity: AppCompatActivity,
    val binding: ActivitySignUpBinding,
)

class SignUpViewModel(private val factory: SignUpFactory) : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    fun signUp(username: String, email: String, password: String) {
        factory.activity.hideKeyboard()

        Log.d("--sign_up--", "username: $username, email: $email, password: $password")

        if (TextUtils.isEmpty(username)) {
            factory.binding.layoutUsername.error = "Please enter your username"
            return
        } else if (TextUtils.isEmpty(email)) {
            factory.binding.layoutEmail.error = "Please enter your email"
            return
        } else if(!isEmailValid(email)) {
            factory.binding.layoutEmail.error = "Please enter valid email address"
            return
        } else if (TextUtils.isEmpty(password)) {
            factory.binding.layoutPassword.error = "Please enter your password"
            return
        } else if (password.length < 6) {
            factory.binding.layoutPassword.error = "Please enter your at least 6 character password"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val firebaseUser = firebaseRepository.signUpWithEmailAndPassword(email, password)

            viewModelScope.launch(Dispatchers.Main) {
                if (firebaseUser != null) {
                    val intent = Intent(factory.activity, LoginActivity::class.java)
                    factory.activity.startActivity(intent)
                    factory.activity.finish()
                } else {
                    factory.activity.toast("Login Failed ...!", Toast.LENGTH_SHORT)
                }
            }
        }
    }

}