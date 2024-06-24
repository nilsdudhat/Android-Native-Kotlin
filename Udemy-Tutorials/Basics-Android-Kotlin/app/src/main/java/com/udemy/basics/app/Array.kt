package com.udemy.basics.app

fun main() {
    val array = arrayOf("Windows", "Android", "MacOS", "Linux")

    val firstElement = array[0]
    println(firstElement)

    println()
    println()

    array[2] = "iOS"

    val elementAt2 = array[2]
    println(elementAt2)

    println()
    println()

    val size = array.size
    println("Size of Array is $size")

    println()
    println()

    for (name in array) {
        println(name)
    }

    println()
    println()

    array.forEach {
        println(it)
    }
}