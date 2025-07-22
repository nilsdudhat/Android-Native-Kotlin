package com.belive.dating.helpers.helper_functions.aes

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object AESUtils {
    private var keyValue: ByteArray? = null
    var hashkey = ""

    @Throws(Exception::class)
    fun Logd(encrypted: String): String {
        hashkey = "MyActivity.class"
        keyValue = hashkey.toByteArray()
        val enc = toByte(encrypted)
        val result = LogE(enc)
        return String(result)
    }

    @Throws(Exception::class)
    private fun LogE(encrypted: ByteArray): ByteArray {
        val skeySpec: SecretKey =
            SecretKeySpec(keyValue, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec)
        return cipher.doFinal(encrypted)
    }

    fun toByte(hexString: String): ByteArray {
        val len = hexString.length / 2
        val result = ByteArray(len)
        for (i in 0 until len) result[i] = Integer.valueOf(
            hexString.substring(2 * i, 2 * i + 2),
            16
        ).toByte()
        return result
    }
}