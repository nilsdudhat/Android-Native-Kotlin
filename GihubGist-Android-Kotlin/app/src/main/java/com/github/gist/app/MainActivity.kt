package com.github.gist.app

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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

        val url = "https://gist.githubusercontent.com/nilsdudhat/"
        val encryptedUrl = url.encryptCBC()
        Log.d("--url--", "encrypted: $encryptedUrl")
        val decryptedUrl = encryptedUrl.decryptCBC()
        Log.d("--url--", "decrypted: $decryptedUrl")
    }
}

val SECRET_KEY = "secretKey"
val SECRET_IV = "secretIV"

private fun String.encryptCBC(): String {
    val iv = IvParameterSpec(SECRET_IV.toByteArray())
    val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
    val crypted = cipher.doFinal(this.toByteArray())
    val encodedByte = Base64.encode(crypted, Base64.DEFAULT)
    return String(encodedByte)
}

private fun String.decryptCBC(): String {
    val decodedByte: ByteArray = Base64.decode(this, Base64.DEFAULT)
    val iv = IvParameterSpec(SECRET_IV.toByteArray())
    val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
    val output = cipher.doFinal(decodedByte)
    return String(output)
}