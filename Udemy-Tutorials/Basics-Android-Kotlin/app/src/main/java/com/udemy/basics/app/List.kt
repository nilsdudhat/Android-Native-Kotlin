package com.udemy.basics.app

fun main() {
    /* Immutable: cannot chane below list value */
    val list = listOf("Apple", "Banana", "Orange", "Mango")

    println("element at 2nd position is ${list[2]}")

    /* Mutable: can change below list value */
    val mutableList = mutableListOf("Apple", "Banana", "Orange", "Mango")
    mutableList.add("Kiwi")
    mutableList.removeAt(1)
    mutableList.remove("Apple")
    mutableList[2] = "Guava"
    mutableList.add(1, "Watermelon")

    for (item in mutableList) {
        println(item)
    }
}