package com.udemy.basics.app

fun main() {
    /* Set: is a collection of unordered elements without duplicates */

    /* Immutable Set: duplicates will automatically removed */
    val fruits = setOf("Apple", "Banana", "Orange", "Mango", "Apple")

    for (fruit in fruits) {
        println(fruit)
    }

    println()
    println()

    /* Mutable Set: cannot update element, cannot insert element at specific index */
    val mutableFruits = mutableSetOf("Apple", "Banana", "Orange", "Mango", "Apple")
    mutableFruits.add("Kiwi")
    mutableFruits.remove("Apple")

    for (fruit in mutableFruits) {
        println(fruit)
    }
}