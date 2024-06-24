package com.udemy.basics.app

fun main() {
    /* Map : is a collection of key-value pairs
    * Keys are unique and cannot be duplicated
    * Values can be duplicated
     */

    /* Immutable map: cannot update element, cannot insert element at specific index */
    val fruits = mapOf("Apple" to 6, "Banana" to 5, "Orange" to 12)

    val numberOfBananas = fruits["Banana"]
    println("no of available banana is $numberOfBananas")

    val mutableFruits = mutableMapOf("Apple" to 12, "Banana" to 8, "Orange" to 6)

    mutableFruits.remove("Apple")
    mutableFruits["Kiwi"] = 7
    mutableFruits["Banana"] = 10

    for (fruit in mutableFruits) {
        println("${fruit.key} : ${fruit.value}")
    }
}