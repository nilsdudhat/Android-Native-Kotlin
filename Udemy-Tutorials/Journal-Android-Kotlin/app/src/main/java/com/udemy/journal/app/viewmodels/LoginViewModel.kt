package com.udemy.journal.app.viewmodels

import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udemy.journal.app.activities.JournalsActivity
import com.udemy.journal.app.activities.SignUpActivity
import com.udemy.journal.app.databinding.ActivityLoginBinding
import com.udemy.journal.app.repositories.FirebaseRepository
import com.udemy.journal.app.utils.hideKeyboard
import com.udemy.journal.app.utils.isEmailValid
import com.udemy.journal.app.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class LoginFactory(
    val activity: AppCompatActivity,
    val binding: ActivityLoginBinding,
)

class LoginViewModel(private val factory: LoginFactory) : ViewModel() {

    private val firebaseRepository: FirebaseRepository = FirebaseRepository()

    fun signIn(email: String, password: String) {
        if (TextUtils.isEmpty(email)) {
            factory.binding.layoutEmail.error = "Please enter your email"
            return
        } else if (!isEmailValid(email)) {
            factory.binding.layoutEmail.error = "Please enter valid email address"
            return
        } else if (TextUtils.isEmpty(password)) {
            factory.binding.layoutPassword.error = "Please enter your password"
            return
        } else if (password.length < 6) {
            factory.binding.layoutPassword.error =
                "Please enter your at least 6 character password"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val firebaseUser = firebaseRepository.signInWithEmailAndPassword(email, password)

            viewModelScope.launch(Dispatchers.Main) {
                if (firebaseUser != null) {
                    // Login Success
                    val intent = Intent(factory.activity, JournalsActivity::class.java)
                    factory.activity.startActivity(intent)
                } else {
                    // Login Failed
                    factory.activity.toast("Login Failed ...!", Toast.LENGTH_SHORT)
                }
            }
        }
    }

    fun signUp() {
        factory.activity.hideKeyboard()

        val intent = Intent(factory.activity, SignUpActivity::class.java)
        factory.activity.startActivity(intent)
        factory.activity.finish()
    }
}